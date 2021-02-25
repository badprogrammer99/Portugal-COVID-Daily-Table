package com.gmail.etpr99.jose.coviddailytable.tables;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;
import com.gmail.etpr99.jose.coviddailytable.models.GeographicZone;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusDays;
import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusWeeks;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.round;

public class HTMLCovidDataTableBuilder extends AbstractCovidDataTableBuilder {
    public HTMLCovidDataTableBuilder(CovidDataSource covidDataSource, Date tableDate) {
        super(covidDataSource, tableDate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CovidDataSource getCovidDataSource() {
        return covidDataSource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NumberFormat getNumberFormatter() {
        return NumberFormat.getInstance(new Locale("sk", "SK"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getTableDate() {
        return tableDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildStatsRow(double todayStat, BiFunction<CovidStats, CovidStats, Integer> statDiffFunction, BiFunction<CovidStats, CovidStats, Double> statPercentVariationFunction) {
        StringBuilder statsRow = new StringBuilder();
        statsRow.append("<td align=\"right\">")
                .append(getNumberFormatter().format(todayStat))
                .append("</td>\n");

        try {
            double statDiff = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate, statDiffFunction);

            statsRow.append("<td align=\"right\">")
                    .append(Math.signum(statDiff) > 0.0 ? "+" : "")
                    .append(getNumberFormatter().format(statDiff))
                    .append("</td>\n");

            for (int stepDay = 1; stepDay <= 7; stepDay += 2) {
                double percentVariation = covidDataSource.getStatsPercentVariationInDays(minusDays(tableDate, stepDay), tableDate, statPercentVariationFunction);
                statsRow.append("<td align=\"right\">")
                        .append(Math.signum(percentVariation) > 0.0 ? "+" : "")
                        .append(percentVariation)
                        .append("%")
                        .append("</td>\n");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return statsRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildNewCasesIncreaseOverActivesRow() {
        StringBuilder newCasesIncreaseOverActivesRow = new StringBuilder();

        try {
            newCasesIncreaseOverActivesRow.append("+");
            newCasesIncreaseOverActivesRow.append(covidDataSource.getStatsPercentVariationInDays(datePreviousToTableDate, tableDate, (stat1, stat2) -> {
                int caseDiff = stat2.getCases() - stat1.getCases();
                return (stat1.getActive() + caseDiff) / (double) stat1.getActive();
            }));
            newCasesIncreaseOverActivesRow.append("%\n");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return newCasesIncreaseOverActivesRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildMovingAverageDaysHeader() {
        StringBuilder movingAverageDaysRow = new StringBuilder();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MMM/yy");

        for (int day = 6; day >= 0; day--) {
            movingAverageDaysRow.append("<th align=\"center\">")
                    .append("<strong>")
                    .append(formatter.format(minusDays(tableDate, day)))
                    .append("</strong>")
                    .append("</th>\n");
        }

        return movingAverageDaysRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildStatMovingAverageRow(BiFunction<CovidStats, CovidStats, Double> pairMapFunction) {
        StringBuilder statMovingAverageRow = new StringBuilder();

        for (int day = 6; day >= 0; day--) {
            try {
                statMovingAverageRow.append("<td align=\"center\">")
                        .append(covidDataSource.getMovingAverageForStatsInDay(minusDays(tableDate, day), pairMapFunction))
                        .append("</td>\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        statMovingAverageRow.append("\n");
        return statMovingAverageRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildMultiWeekCaseVariationRow(GeographicZone geographicZone) {
        StringBuilder regionalStatVariationRow = new StringBuilder();

        try {
            int regionalCaseDiffInTableDate = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate,
                    (stat1, stat2) -> stat2.getRegionalCasesMap().get(geographicZone)[0] - stat1.getRegionalCasesMap().get(geographicZone)[0]);

            for (int week = 1; week <= 4; week++) {
                Date dayOfPreviousWeek = minusWeeks(tableDate, week);
                Date dayBeforeDayOfPreviousWeek = minusDays(minusWeeks(tableDate, week), 1);

                int regionalCaseDiffXWeeksAgoRelativeToTableDate = covidDataSource.getStatsDiffInDays(dayBeforeDayOfPreviousWeek,
                        dayOfPreviousWeek,
                        (stat1, stat2) -> stat2.getRegionalCasesMap().get(geographicZone)[0] - stat1.getRegionalCasesMap().get(geographicZone)[0]);

                regionalStatVariationRow.append("<td align=\"right\">")
                        .append(Math.signum(regionalCaseDiffInTableDate - regionalCaseDiffXWeeksAgoRelativeToTableDate) > 0.0 ? "+" : "")
                        .append(getNumberFormatter().format(regionalCaseDiffInTableDate - regionalCaseDiffXWeeksAgoRelativeToTableDate))
                        .append("</td>\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        regionalStatVariationRow.append("\n");
        return regionalStatVariationRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildRegionsInfluencePercentOnGlobalStatRow(StatType statType) {
        StringBuilder regionInfluencePercentOnGlobalStatRow = new StringBuilder();

        Map<GeographicZone, int[]> regionalCases = sevenDayCovidData.get(tableDate).getRegionalCasesMap();
        List<GeographicZone> geographicZones = regionalCases.keySet()
                .stream()
                .filter(zone -> !zone.equals(GeographicZone.COUNTRYWIDE))
                .collect(Collectors.toList());

        try {
            int globalStatDiffToday = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate, (stat1, stat2) -> {
                int todayRegionalStats = stat2.getRegionalCasesMap().get(GeographicZone.COUNTRYWIDE)[statType.getNumVal()];
                int yesterdayRegionalStats = stat1.getRegionalCasesMap().get(GeographicZone.COUNTRYWIDE)[statType.getNumVal()];

                return todayRegionalStats - yesterdayRegionalStats;
            });

            for (GeographicZone zone: geographicZones) {
                int regionStatDiffToday = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate, (stat1, stat2) -> {
                    int todayRegionalStats = stat2.getRegionalCasesMap().get(zone)[statType.getNumVal()];
                    int yesterdayRegionalStats = stat1.getRegionalCasesMap().get(zone)[statType.getNumVal()];

                    return todayRegionalStats - yesterdayRegionalStats;
                });

                regionInfluencePercentOnGlobalStatRow.append("<td align=\"center\">")
                        .append(round((regionStatDiffToday / (double) globalStatDiffToday) * 100, 2))
                        .append("%")
                        .append("</td>\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return regionInfluencePercentOnGlobalStatRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildTable() {
        return prettyPrintHtml("<div class=\"md\">\n" +
                "   <h1>ATUALIZAÇÃO DIÁRIA - " + dayOfDate + " de " + printMonthNameInPortuguese() + " de " + yearOfDate + "</h1>\n" +
                "   <hr/>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"left\"></th>\n" +
                "            <th align=\"right\">Valor</th>\n" +
                "            <th align=\"right\">Variação</th>\n" +
                "            <th align=\"right\">\uD83D\uDCC8 1 dia</th>\n" +
                "            <th align=\"right\">\uD83D\uDCC8 3 dias</th>\n" +
                "            <th align=\"right\">\uD83D\uDCC8 5 dias</th>\n" +
                "            <th align=\"right\">\uD83D\uDCC8 7 dias</th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>\uD83D\uDC65 Casos Confirmados</strong></td>\n" +
                "            " + buildStatsRow(casesInTableDate,
                                (stat1, stat2) -> stat2.getCases() - stat1.getCases(),
                                (stat1, stat2) -> stat2.getCases() / (double) stat1.getCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>✔️ Recuperados</strong></td>\n" +
                "            " + buildStatsRow(recoveriesInTableDate,
                                (stat1, stat2) -> stat2.getRecoveries() - stat1.getRecoveries(),
                                (stat1, stat2) -> stat2.getRecoveries() / (double) stat1.getRecoveries()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>☠️ Óbitos</strong></td>\n" +
                "            " + buildStatsRow(deathsInTableDate,
                                (stat1, stat2) -> stat2.getDeaths() - stat1.getDeaths(),
                                (stat1, stat2) -> stat2.getDeaths() / (double) stat1.getDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>\uD83C\uDFE5 Internados</strong></td>\n" +
                "            " + buildStatsRow(hospitalizedInTableDate,
                                (stat1, stat2) -> stat2.getHospitalized() - stat1.getHospitalized(),
                                (stat1, stat2) -> stat2.getHospitalized() / (double) stat1.getHospitalized()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>\uD83D\uDECC UCI</strong></td>\n" +
                "            " + buildStatsRow(icuInTableDate,
                                (stat1, stat2) -> stat2.getIcu() - stat1.getIcu(),
                                (stat1, stat2) -> stat2.getIcu() / (double) stat1.getIcu()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>\uD83D\uDE37 Casos Ativos</strong></td>\n" +
                "            " + buildStatsRow(activeInTableDate,
                                (stat1, stat2) -> stat2.getActive() - stat1.getActive(),
                                (stat1, stat2) -> stat2.getActive() / (double) stat1.getActive()) +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <hr/>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"center\">\uD83D\uDCCA <strong>Aumento de Novos Casos face a Casos Ativos:</strong></th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\">" + buildNewCasesIncreaseOverActivesRow() + "</td>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <hr/>\n" +
                "   <h2>Média Móvel a 7 dias</h2>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"center\"><strong>Novos</strong></th>\n" +
                "            " + buildMovingAverageDaysHeader() +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\"><strong>\uD83D\uDC65</strong></td>\n" +
                "            " + buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getCases() - stat1.getCases())) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"center\"><strong>✔️</strong></td>\n" +
                "            " + buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getRecoveries() - stat1.getRecoveries())) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"center\"><strong>☠️</strong></td>\n" +
                "            " + buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getDeaths() - stat1.getDeaths())) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"center\"><strong>\uD83C\uDFE5</strong></td>\n" +
                "           " + buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getHospitalized() - stat1.getHospitalized())) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"center\"><strong>\uD83D\uDECC</strong></td>\n" +
                "           " + buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getIcu() - stat1.getIcu())) +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <hr/>\n" +
                "   <h2>Variação de novos casos confirmados em relação ao mesmo dia de semanas anteriores</h2>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"left\"></th>\n" +
                "            <th align=\"right\"><strong>1 sem</strong></th>\n" +
                "            <th align=\"right\"><strong>2 sem</strong></th>\n" +
                "            <th align=\"right\"><strong>3 sem</strong></th>\n" +
                "            <th align=\"right\"><strong>4 sem</strong></th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Norte</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.NORTE) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Centro</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.CENTRO) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>LVT</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.LISBOA_E_VALE_DO_TEJO) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Alentejo</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.ALENTEJO) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Algarve</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.ALGARVE) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Açores</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.ACORES) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Madeira</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.MADEIRA) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Geral</strong></td>\n" +
                "            " + buildMultiWeekCaseVariationRow(GeographicZone.COUNTRYWIDE) +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <hr/>\n" +
                "   <h2>Por região</h2>\n" +
                "   <p><strong>Casos Confirmados</strong></p>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"left\"><strong>Região</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDC65 Totais</strong></th>\n" +
                "            <th align=\"right\"><strong>Variação</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 1 dia</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 3 dias</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 5 dias</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 7 dias</strong></th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Norte</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getNorthCases(),
                                (stat1, stat2) -> stat2.getNorthCases() - stat1.getNorthCases(),
                                (stat1, stat2) -> stat2.getNorthCases() / (double) stat1.getNorthCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Centro</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getCenterCases(),
                                (stat1, stat2) -> stat2.getCenterCases() - stat1.getCenterCases(),
                                (stat1, stat2) -> stat2.getCenterCases() / (double) stat1.getCenterCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>LVT</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getLvtCases(),
                                (stat1, stat2) -> stat2.getLvtCases() - stat1.getLvtCases(),
                                (stat1, stat2) -> stat2.getLvtCases() / (double) stat1.getLvtCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Alentejo</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAlentejoCases(),
                                (stat1, stat2) -> stat2.getAlentejoCases() - stat1.getAlentejoCases(),
                                (stat1, stat2) -> stat2.getAlentejoCases() / (double) stat1.getAlentejoCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Algarve</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAlentejoCases(),
                                (stat1, stat2) -> stat2.getAlgarveCases() - stat1.getAlgarveCases(),
                                (stat1, stat2) -> stat2.getAlgarveCases() / (double) stat1.getAlgarveCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Açores</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAzoresCases(),
                                (stat1, stat2) -> stat2.getAzoresCases() - stat1.getAzoresCases(),
                                (stat1, stat2) -> stat2.getAzoresCases() / (double) stat1.getAzoresCases()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Madeira</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getMadeiraCases(),
                                (stat1, stat2) -> stat2.getMadeiraCases() - stat1.getMadeiraCases(),
                                (stat1, stat2) -> stat2.getMadeiraCases() / (double) stat1.getMadeiraCases()) +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <p><strong>Óbitos</strong></p>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"left\"><strong>Região</strong></th>\n" +
                "            <th align=\"right\"><strong>☠️ Óbitos</strong></th>\n" +
                "            <th align=\"right\"><strong>Variação</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 1 dia</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 3 dias</strong></th>\n" +
                "            <th align=\"right\"><strong>\uD83D\uDCC8 5 dias</strong></th>\n" +
                "            <th align=\"right\">\uD83D\uDCC8 7 dias</th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Norte</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getNorthDeaths(),
                            (stat1, stat2) -> stat2.getNorthDeaths() - stat1.getNorthDeaths(),
                            (stat1, stat2) -> stat2.getNorthDeaths() / (double) stat1.getNorthDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Centro</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getCenterDeaths(),
                                (stat1, stat2) -> stat2.getCenterDeaths() - stat1.getCenterDeaths(),
                                (stat1, stat2) -> stat2.getCenterDeaths() / (double) stat1.getCenterDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>LVT</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getLvtDeaths(),
                                (stat1, stat2) -> stat2.getLvtDeaths() - stat1.getLvtDeaths(),
                                (stat1, stat2) -> stat2.getLvtDeaths() / (double) stat1.getLvtDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Alentejo</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAlentejoDeaths(),
                                (stat1, stat2) -> stat2.getAlentejoDeaths() - stat1.getAlentejoDeaths(),
                                (stat1, stat2) -> stat2.getAlentejoDeaths() / (double) stat1.getAlentejoDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Algarve</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAlgarveDeaths(),
                                (stat1, stat2) -> stat2.getAlgarveDeaths() - stat1.getAlgarveDeaths(),
                                (stat1, stat2) -> stat2.getAlgarveDeaths() / (double) stat1.getAlgarveDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Açores</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getAzoresDeaths(),
                                (stat1, stat2) -> stat2.getAzoresDeaths() - stat1.getAzoresDeaths(),
                                (stat1, stat2) -> stat2.getAzoresDeaths() / (double) stat1.getAzoresDeaths()) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"left\"><strong>Madeira</strong></td>\n" +
                "            " + buildStatsRow(sevenDayCovidData.get(tableDate).getMadeiraDeaths(),
                                (stat1, stat2) -> stat2.getMadeiraDeaths() - stat1.getMadeiraDeaths(),
                                (stat1, stat2) -> stat2.getMadeiraDeaths() / (double) stat1.getMadeiraDeaths()) +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <p><strong>Proporção de novos casos e de óbitos por ARS</strong></p>\n" +
                "   <table>\n" +
                "      <thead>\n" +
                "         <tr>\n" +
                "            <th align=\"center\"></th>\n" +
                "            <th align=\"center\"><strong>Norte</strong></th>\n" +
                "            <th align=\"center\"><strong>Centro</strong></th>\n" +
                "            <th align=\"center\"><strong>LVT</strong></th>\n" +
                "            <th align=\"center\"><strong>Alentejo</strong></th>\n" +
                "            <th align=\"center\"><strong>Algarve</strong></th>\n" +
                "            <th align=\"center\"><strong>Madeira</strong></th>\n" +
                "            <th align=\"center\"><strong>Açores</strong></th>\n" +
                "         </tr>\n" +
                "      </thead>\n" +
                "      <tbody>\n" +
                "         <tr>\n" +
                "            <td align=\"center\">Novos Casos</td>\n" +
                "            " + buildRegionsInfluencePercentOnGlobalStatRow(StatType.CASES) +
                "         </tr>\n" +
                "         <tr>\n" +
                "            <td align=\"center\">Novos Óbitos</td>\n" +
                "            " + buildRegionsInfluencePercentOnGlobalStatRow(StatType.DEATHS) +
                "         </tr>\n" +
                "         </tr>\n" +
                "      </tbody>\n" +
                "   </table>\n" +
                "   <p><strong>Dados obtidos automaticamente do <em>site</em> do <a href=\"https://covid19.min-saude.pt/relatorio-de-situacao/\">Ministério da Saúde dedicado à COVID-19</a>.</strong></p>\n" +
                "   <p><em>Script</em> original por BarcaDoInferno e hsamtronp.</p>\n" +
                "</div>");
    }

    private String prettyPrintHtml(String rawHtml) {
        Document doc = Jsoup.parseBodyFragment(rawHtml);
        doc.outputSettings().indentAmount(4);
        return doc.body().html();
    }
}
