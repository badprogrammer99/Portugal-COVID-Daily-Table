package com.gmail.etpr99.jose.coviddailytable.tablebuilders;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;
import com.gmail.etpr99.jose.coviddailytable.models.GeographicZone;

import java.text.NumberFormat;
import java.util.Date;
import java.util.function.BiFunction;

public interface CovidDataTableBuilder {

    /**
     * @return The COVID data source to be used with this table builder.
     */
    CovidDataSource getCovidDataSource();

    /**
     * @return Gets the {@link NumberFormat} to be used when formatting numbers output.
     */
    NumberFormat getNumberFormatter();

    /**
     * @return The date of the table.
     */
    Date getTableDate();

    /**
     * Sets the date of the table.
     */
    void setTableDate(Date tableDate);

    /**
     * Builds a row of a certain stat that can be represented in a {@link CovidStats}.
     * @param todayStat Today's stat.
     * @param statDiffFunction The function to be applied when calculating the difference between stats of different days.
     * @param statPercentVariationFunction The function to be applied when calculating the percent variation between stats of different days.
     * @return A built row of a certain stat.
     */
    String buildStatsRow(double todayStat,
                         BiFunction<CovidStats, CovidStats, Integer> statDiffFunction,
                         BiFunction<CovidStats, CovidStats, Double> statPercentVariationFunction);

    /**
     * @return The new cases increase over actives percentage row.
     */
    String buildNewCasesIncreaseOverActivesRow();

    /**
     * @return The moving average days header.
     */
    String buildMovingAverageDaysHeader();

    /**
     * Builds a moving average row of a certain stat that can be represented in a {@link CovidStats}.
     * @param pairMapFunction The function to be used when mapping the key-value entries of a map.
     * @return A built moving average row of a certain stat.
     */
    String buildStatMovingAverageRow(BiFunction<CovidStats, CovidStats, Double> pairMapFunction);

    /**
     * Builds the multi week case variation row in a {@link GeographicZone}.
     * @param geographicZone The geographic zone for which to build the multi week case variation.
     * @return The built multi week case variation row.
     */
    String buildMultiWeekCaseVariationRow(GeographicZone geographicZone);

    /**
     * Builds the regions influence percentage on a countrywide stat row.
     * @param statType The stat type for which to compute the regions influence percentage.
     * @return The built regions influence percentage on a countrywide stat row
     */
    String buildRegionsInfluencePercentOnGlobalStatRow(StatType statType);

    /**
     * @return The final built table.
     */
    String buildTable();

    /**
     * @return The endpoint used to access the results of this table builder.
     */
    String endpoint();

    enum StatType {
        CASES(0),
        DEATHS(1);

        private int numVal;

        StatType(int numVal) {
            this.numVal = numVal;
        }

        public int getNumVal() {
            return numVal;
        }
    }
}
