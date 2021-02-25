package com.gmail.etpr99.jose.coviddailytable.datasources;

import com.gmail.etpr99.jose.coviddailytable.annotations.Retryable;
import com.gmail.etpr99.jose.coviddailytable.exceptions.ReportInDateNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.exceptions.TodayReportNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;
import com.gmail.etpr99.jose.coviddailytable.models.ExpiringObject;
import com.google.common.base.Supplier;
import com.opencsv.bean.CsvToBeanBuilder;

import one.util.streamex.EntryStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.*;
import static com.gmail.etpr99.jose.coviddailytable.utils.MiscUtils.getStatForPdfRegion;
import static com.gmail.etpr99.jose.coviddailytable.utils.MiscUtils.throwCheckedException;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.formatStatPercentVariation;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.round;

@Component
public class CovidDataSourceImpl implements CovidDataSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(CovidDataSourceImpl.class);

    // ----------- CACHE ----------- //
    private final Supplier<Document> dgsPage;
    private ExpiringObject<CovidStats> todayCovidStats;
    private ExpiringObject<List<CovidStats>> parsedCsvStats;
    // ----------- CACHE ----------- //

    @Autowired
    public CovidDataSourceImpl(@Qualifier("dgsPage") Supplier<Document> dgsPage) {
        this.dgsPage = dgsPage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CovidStats getTodayCovidReportData() {
        if (todayCovidStats != null && todayCovidStats.get() != null) return todayCovidStats.get();

        try {
            Element mostRecentReportAnchor = dgsPage.get().getElementsByClass("single_content").select("a[href]").get(0);
            String formattedTodayDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

            boolean isReportFromToday = mostRecentReportAnchor.html().contains(formattedTodayDate);
            if (!isReportFromToday) throw new TodayReportNonExistentException("Today's report still doesn't exist!");

            URLConnection downloadConnection = new URL(mostRecentReportAnchor.attr("href")).openConnection();
            Date downloadConnDate = dateWithoutTime(new Date(downloadConnection.getLastModified()));

            LOGGER.info("Downloading today's report data!");

            PDDocument dgsReport = PDDocument.load(downloadConnection.getInputStream());
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();

            for (Map.Entry<String, Rectangle2D.Double> coordinate: COORDINATES.entrySet())
                stripper.addRegion(coordinate.getKey(), coordinate.getValue());

            stripper.extractRegions(dgsReport.getPage(0));

            todayCovidStats = new ExpiringObject<>(new CovidStats(), timeRemainingUntilTomorrow(), TimeUnit.MILLISECONDS);
            todayCovidStats.get().setDate(downloadConnDate);

            Class<? extends CovidStats> covidStatsClazz = todayCovidStats.get().getClass();

            for (String region: COORDINATES.keySet()) {
                Field field = covidStatsClazz.getDeclaredField(region);
                field.setAccessible(true);
                field.set(todayCovidStats.get(), getStatForPdfRegion(stripper, region));
            }

            dgsReport.close();
        } catch (IllegalAccessException | IOException | NoSuchFieldException e) {
            throwCheckedException(e);
        }

        return todayCovidStats.get();
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
        getParsedCsvCovidStats().stream()
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

    @Retryable
    private List<CovidStats> getParsedCsvCovidStats() {
        if (parsedCsvStats != null && parsedCsvStats.get() != null) return parsedCsvStats.get();

        LOGGER.info("Fetching a new instance of the COVID-19 CSV stats...");

        try (BufferedReader downloadStreamReader = new BufferedReader(new InputStreamReader(
            new URL("https://raw.githubusercontent.com/dssg-pt/covid19pt-data/master/data.csv").openConnection().getInputStream()
        ))) {
            parsedCsvStats = new ExpiringObject<>(new CsvToBeanBuilder<CovidStats>(downloadStreamReader)
                    .withType(CovidStats.class)
                    .build()
                    .parse(), timeRemainingUntilTomorrow(), TimeUnit.MILLISECONDS);
        } catch (IOException e) {
            throwCheckedException(e);
        }

        return parsedCsvStats.get();
    }
}
