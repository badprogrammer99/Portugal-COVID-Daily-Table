package com.gmail.etpr99.jose.coviddailytable.datasources;

import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;

import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Interface representing a covid data source.
 */
public interface CovidDataSource {

    // ------------------- COORDINATES OF EACH STAT RECTANGLE IN THE DGS REPORT PDF ------------------- //

    Map<String, Rectangle2D.Double> COORDINATES = new LinkedHashMap<>(){{
        put("cases", new Rectangle2D.Double(34.0, 476.35, 60.0, 20.0));
        put("recoveries", new Rectangle2D.Double(34.0, 276.77, 60.0, 20.0));
        put("deaths", new Rectangle2D.Double(34.0, 342.89, 60.0, 20.0));
        put("hospitalized", new Rectangle2D.Double(39.0, 699.4504, 35.0, 20.0));
        put("icu", new Rectangle2D.Double(155.0, 699.4504, 35.0, 20.0));
        put("active", new Rectangle2D.Double(34.0, 207.61040000000003, 60.0, 20.0));
        put("northCases", new Rectangle2D.Double(420.0, 200.89999999999998, 35.0, 20.0));
        put("northDeaths", new Rectangle2D.Double(425.0, 220.93999999999994, 35.0, 20.0));
        put("centerCases", new Rectangle2D.Double(407.0, 311.80999999999995, 35.0, 20.0));
        put("centerDeaths", new Rectangle2D.Double(414.0, 332.44039999999995, 35.0, 20.0));
        put("lvtCases", new Rectangle2D.Double(345.0, 420.16999999999996, 35.0, 20.0));
        put("lvtDeaths", new Rectangle2D.Double(351.0, 448.0404, 35.0, 20.0));
        put("alentejoCases", new Rectangle2D.Double(396.0, 506.71, 35.0, 20.0));
        put("alentejoDeaths", new Rectangle2D.Double(405.0, 526.63, 35.0, 20.0));
        put("algarveCases", new Rectangle2D.Double(386.0, 592.27, 35.0, 20.0));
        put("algarveDeaths", new Rectangle2D.Double(392.0, 612.43, 35.0, 20.0));
        put("azoresCases", new Rectangle2D.Double(234.0, 205.45039999999995, 35.0, 20.0));
        put("azoresDeaths", new Rectangle2D.Double(242.00, 224.89999999999998, 35.0, 20.0));
        put("madeiraCases", new Rectangle2D.Double(235.00, 351.77, 35.0, 20.0));
        put("madeiraDeaths", new Rectangle2D.Double(241.00, 372.65, 35.0, 20.0));
    }};

    // ------------------- COORDINATES OF EACH STAT RECTANGLE IN THE DGS REPORT PDF ------------------- //

    /**
     * Gets the data from the present day COVID report.
     * @return The COVID data from the present day COVID report.
     */
    CovidStats getTodayCovidReportData();

    /**
     * Gets COVID data pertaining to a specific date.
     * @param day The date of the COVID stats we want to get information from.
     * @return The COVID data in the specified date.
     */
    CovidStats getCovidDataInDay(Date day);

    /**
     * Gets all COVID data within the specified timeframe.
     * @param firstDay The lower bound date for searching the COVID data. For example, the date 17-02-2021.
     * @param secondDay The upper bound date for searching the COVID data. For example, the date 19-02-2021.
     * If we pass the 17-02-2021 date as the firstDay argument, and the 19-02-2021 date as the secondDay argument,
     * then the method will retrieve the COVID data from 17-02-2021, the COVID data from 18-02-2021, and the COVID data
     * from 19-02-2021.
     * @return The COVID data within the specified timeframe.
     */
    Map<Date, CovidStats> getCovidDataInbetweenDays(Date firstDay, Date secondDay);

    /**
     * Gets the difference between stats in certain days.
     * @param day1 Day parameter used to retrieve the stats of a certain day (the day passed in the day1 parameter).
     * @param day2 Day parameter used to retrieve the stats of a certain day (the day passed in the day2 parameter).
     * @param statDiffFunction The function to apply when performing the difference between stats in certain days.
     * @return The difference between the stats.
     */
    int getStatsDiffInDays(Date day1, Date day2, BiFunction<CovidStats, CovidStats, Integer> statDiffFunction);

    /**
     * Gets the percent variation between stats in certain days.
     * @param day1 Day parameter used to retrieve the stats of a certain day (the day passed in the day1 parameter).
     * @param day2 Day parameter used to retrieve the stats of a certain day (the day passed in the day2 parameter).
     * @param statPercentVariationFunction The function to apply when performing the percent variation between stats in certain days.
     * @return The percent variation between the stats.
     */
    double getStatsPercentVariationInDays(Date day1, Date day2,
                                          BiFunction<CovidStats, CovidStats, Double> statPercentVariationFunction);

    /**
     * Gets the moving average for a certain stat in a certain day. The moving average of a stat in a certain day is obtained
     * by processing
     * @param day The day for which we want to check the moving average.
     * @param pairMapFunction The BiFunction used to transform the map of COVID data obtained in the
     * {@link #getCovidDataInbetweenDays(Date, Date)}.
     * @return The moving average of a certain stat in a certain day.
     */
    double getMovingAverageForStatsInDay(Date day, BiFunction<CovidStats, CovidStats, Double> pairMapFunction);
}
