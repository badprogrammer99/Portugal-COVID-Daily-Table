package com.gmail.etpr99.jose.coviddailytable.datasources;

import com.gmail.etpr99.jose.coviddailytable.exceptions.ReportInDateNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.exceptions.TodayReportNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;
import com.opencsv.bean.CsvToBeanBuilder;

import one.util.streamex.EntryStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.*;
import static com.gmail.etpr99.jose.coviddailytable.utils.MiscUtils.getStatForPdfRegion;
import static com.gmail.etpr99.jose.coviddailytable.utils.MiscUtils.throwCheckedException;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.formatStatPercentVariation;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.round;

public class CovidDataSourceImpl implements CovidDataSource {
    private Document dgsPage;
    private CovidStats todayCovidStats;
    private List<CovidStats> parsedCsvCovidStats;

    public CovidDataSourceImpl() {
        try (BufferedReader downloadStreamReader = new BufferedReader(new InputStreamReader(
            new URL("https://raw.githubusercontent.com/dssg-pt/covid19pt-data/master/data.csv").openConnection().getInputStream()
        ))) {
            dgsPage = Jsoup.connect("https://covid19.min-saude.pt/relatorio-de-situacao/").get();

            parsedCsvCovidStats = new CsvToBeanBuilder<CovidStats>(downloadStreamReader)
                    .withType(CovidStats.class)
                    .build()
                    .parse();
        } catch (IOException e) {
            throwCheckedException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CovidStats getTodayCovidReportData() {
        if (todayCovidStats != null) return todayCovidStats;

        try {
            Element mostRecentReportAnchor = dgsPage.getElementsByClass("single_content").select("a[href]").get(0);
            String formattedTodayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            boolean isReportFromToday = mostRecentReportAnchor.html().contains(formattedTodayDate);
            if (!isReportFromToday) throw new TodayReportNonExistentException("Today's report still doesn't exist!");

            URLConnection downloadConnection = new URL(mostRecentReportAnchor.attr("href")).openConnection();
            Date downloadConnDate = dateWithoutTime(new Date(downloadConnection.getLastModified()));

            PDDocument dgsReport = PDDocument.load(downloadConnection.getInputStream());
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();

            for (Map.Entry<String, Rectangle2D.Double> coordinate: COORDINATES.entrySet())
                stripper.addRegion(coordinate.getKey(), coordinate.getValue());

            stripper.extractRegions(dgsReport.getPage(0));

            todayCovidStats = new CovidStats();
            todayCovidStats.setDate(downloadConnDate);

            Class<? extends CovidStats> covidStatsClazz = todayCovidStats.getClass();

            for (String region: COORDINATES.keySet()) {
                Field field = covidStatsClazz.getDeclaredField(region);
                field.setAccessible(true);
                field.set(todayCovidStats, getStatForPdfRegion(stripper, region));
            }

            dgsReport.close();
        } catch (IllegalAccessException | IOException | NoSuchFieldException e) {
            throwCheckedException(e);
        }

        return todayCovidStats;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CovidStats getCovidDataInDay(Date day) {
        if (areDaysEqual(day, new Date())) return getTodayCovidReportData();
        return getCovidDataInbetweenDays(day, day).get(dateWithoutTime(day));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Date, CovidStats> getCovidDataInbetweenDays(Date firstDay, Date secondDay) {
        Map<Date, CovidStats> covidDataInbetweenDays = new LinkedHashMap<>();

        if (areDaysEqual(secondDay, new Date())) {
            CovidStats todayCovidStats = getTodayCovidReportData();
            covidDataInbetweenDays.put(dateWithoutTime(new Date()), todayCovidStats);
            if (!firstDay.equals(secondDay)) secondDay = minusDays(secondDay, 1);
        }

        Date finalSecondDay = secondDay;
        parsedCsvCovidStats.stream()
            .filter(covidStats -> covidStats.getDate().getTime() >= firstDay.getTime())
            .filter(covidStats -> covidStats.getDate().getTime() <= finalSecondDay.getTime())
            .forEach(covidStats -> covidDataInbetweenDays.put(covidStats.getDate(), covidStats));

        if (covidDataInbetweenDays.get(firstDay) == null || covidDataInbetweenDays.get(secondDay) == null)
            throw new ReportInDateNonExistentException("COVID stats for the specified day(s) do not exist!");

        return new TreeMap<>(covidDataInbetweenDays);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStatsDiffInDays(Date day1, Date day2, BiFunction<CovidStats, CovidStats, Integer> statDiffFunction) {
        CovidStats covidStatsInDay1 = getCovidDataInDay(dateWithoutTime(day1));
        CovidStats covidStatsInDay2 = getCovidDataInDay(dateWithoutTime(day2));
        return statDiffFunction.apply(covidStatsInDay1, covidStatsInDay2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getStatsPercentVariationInDays(Date day1, Date day2,
                                                 BiFunction<CovidStats, CovidStats, Double> statPercentVariationFunction) {
        CovidStats covidStatsInDay1 = getCovidDataInDay(dateWithoutTime(day1));
        CovidStats covidStatsInDay2 = getCovidDataInDay(dateWithoutTime(day2));
        return formatStatPercentVariation(statPercentVariationFunction.apply(covidStatsInDay1, covidStatsInDay2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMovingAverageForStatsInDay(Date day, BiFunction<CovidStats, CovidStats, Double> pairMapFunction) {
        Date dayWithoutTime = dateWithoutTime(day);
        Map<Date, CovidStats> weeklyStatsForDay = getCovidDataInbetweenDays(minusWeeks(dayWithoutTime, 1), dayWithoutTime);
        return round(EntryStream.of(weeklyStatsForDay)
                .values()
                .pairMap(pairMapFunction)
                .mapToDouble(value -> value)
                .average()
                .orElseThrow(() -> new RuntimeException("Could not calculate average for this stat")), 2);
    }
}
