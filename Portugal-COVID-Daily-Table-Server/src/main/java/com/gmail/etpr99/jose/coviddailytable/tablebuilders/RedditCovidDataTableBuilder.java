package com.gmail.etpr99.jose.coviddailytable.tablebuilders;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;
import com.gmail.etpr99.jose.coviddailytable.models.GeographicZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusDays;
import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusWeeks;
import static com.gmail.etpr99.jose.coviddailytable.utils.NumberUtils.round;

@Component
public class RedditCovidDataTableBuilder extends AbstractCovidDataTableBuilder {

    @Autowired
    public RedditCovidDataTableBuilder(CovidDataSource covidDataSource) {
        super(covidDataSource);
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
    public String buildStatsRow(double todayStat,
                                BiFunction<CovidStats, CovidStats, Integer> statDiffFunction,
                                BiFunction<CovidStats, CovidStats, Double> statPercentVariationFunction) {
        StringBuilder statsRow = new StringBuilder();
        statsRow.append(getNumberFormatter().format(todayStat)).append("|");

        double statDiff = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate, statDiffFunction);

        statsRow.append(Math.signum(statDiff) > 0.0 ? "+" : "")
                .append(getNumberFormatter().format(statDiff))
                .append("|");

        for (int stepDay = 1; stepDay <= 7; stepDay += 2) {
            double percentVariation = covidDataSource.getStatsPercentVariationInDays(minusDays(tableDate, stepDay), tableDate, statPercentVariationFunction);
            statsRow.append(Math.signum(percentVariation) > 0.0 ? "+" + percentVariation : percentVariation).append("%|");
        }


        statsRow.append("\n");
        return statsRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildNewCasesIncreaseOverActivesRow() {
        StringBuilder newCasesIncreaseOverActivesRow = new StringBuilder();

        newCasesIncreaseOverActivesRow.append("|\uD83D\uDCCA **Aumento de Novos Casos face a Casos Ativos:**|\n")
                .append(":--:|\n")
                .append("|+");
        newCasesIncreaseOverActivesRow.append(covidDataSource.getStatsPercentVariationInDays(datePreviousToTableDate, tableDate, (stat1, stat2) -> {
            int caseDiff = stat2.getCases() - stat1.getCases();
            return (stat1.getActive() + caseDiff) / (double) stat1.getActive();
        }));
        newCasesIncreaseOverActivesRow.append("%|\n");

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
            movingAverageDaysRow.append("**");
            movingAverageDaysRow.append(formatter.format(minusDays(tableDate, day))).append("**");
            movingAverageDaysRow.append("|");
        }

        movingAverageDaysRow.append("\n");
        return movingAverageDaysRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildStatMovingAverageRow(BiFunction<CovidStats, CovidStats, Double> pairMapFunction) {
        StringBuilder statMovingAverageRow = new StringBuilder();

        for (int day = 6; day >= 0; day--) {
            statMovingAverageRow
                    .append(covidDataSource.getMovingAverageForStatsInDay(minusDays(tableDate, day), pairMapFunction))
                    .append("|");
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

        int regionalCaseDiffToday = covidDataSource.getStatsDiffInDays(datePreviousToTableDate, tableDate,
                (stat1, stat2) -> stat2.getRegionalCasesMap().get(geographicZone)[0] - stat1.getRegionalCasesMap().get(geographicZone)[0]);

        for (int week = 1; week <= 4; week++) {
            Date dayOfPreviousWeek = minusWeeks(tableDate, week);
            Date dayBeforeDayOfPreviousWeek = minusDays(minusWeeks(tableDate, week), 1);

            int regionalCaseDiffXWeeksAgo = covidDataSource.getStatsDiffInDays(dayBeforeDayOfPreviousWeek,
                    dayOfPreviousWeek,
                    (stat1, stat2) -> stat2.getRegionalCasesMap().get(geographicZone)[0] - stat1.getRegionalCasesMap().get(geographicZone)[0]);

            regionalStatVariationRow.append(Math.signum(regionalCaseDiffToday - regionalCaseDiffXWeeksAgo) > 0.0 ? "+" : "");
            regionalStatVariationRow.append(getNumberFormatter().format(regionalCaseDiffToday - regionalCaseDiffXWeeksAgo));
            regionalStatVariationRow.append("|");
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

            regionInfluencePercentOnGlobalStatRow.append(round((regionStatDiffToday / (double) globalStatDiffToday) * 100, 2));
            regionInfluencePercentOnGlobalStatRow.append("%|");
        }

        regionInfluencePercentOnGlobalStatRow.append("\n");
        return regionInfluencePercentOnGlobalStatRow.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String buildTable() {
        return "# ATUALIZAÇÃO DIÁRIA - " + dayOfDate +  " de " + printMonthNameInPortuguese() + " de " + yearOfDate + "\n" +
                "    \n" +
                "---\n" +
                "\n" +
                "||Valor|Variação|\uD83D\uDCC8 1 dia|\uD83D\uDCC8 3 dias|\uD83D\uDCC8 5 dias|\uD83D\uDCC8 7 dias|\n" +
                ":--|--:|--:|--:|--:|--:|--:|\n" +
                "|**\uD83D\uDC65 Casos Confirmados**|" +
                buildStatsRow(casesInTableDate,
                        (stat1, stat2) -> stat2.getCases() - stat1.getCases(),
                        (stat1, stat2) -> stat2.getCases() / (double) stat1.getCases()) +
                "|**✔️ Recuperados**|" +
                buildStatsRow(recoveriesInTableDate,
                        (stat1, stat2) -> stat2.getRecoveries() - stat1.getRecoveries(),
                        (stat1, stat2) -> stat2.getRecoveries() / (double) stat1.getRecoveries()) +
                "|**☠️ Óbitos**|" +
                buildStatsRow(deathsInTableDate,
                        (stat1, stat2) -> stat2.getDeaths() - stat1.getDeaths(),
                        (stat1, stat2) -> stat2.getDeaths() / (double) stat1.getDeaths()) +
                "|**\uD83C\uDFE5 Internados**|" +
                buildStatsRow(hospitalizedInTableDate,
                        (stat1, stat2) -> stat2.getHospitalized() - stat1.getHospitalized(),
                        (stat1, stat2) -> stat2.getHospitalized() / (double) stat1.getHospitalized()) +
                "|**\uD83D\uDECC UCI**|" +
                buildStatsRow(icuInTableDate,
                        (stat1, stat2) -> stat2.getIcu() - stat1.getIcu(),
                        (stat1, stat2) -> stat2.getIcu() / (double) stat1.getIcu()) +
                "|**\uD83D\uDE37 Casos Ativos**|" +
                buildStatsRow(activeInTableDate,
                        (stat1, stat2) -> stat2.getActive() - stat1.getActive(),
                        (stat1, stat2) -> stat2.getActive() / (double) stat1.getActive()) +
                "\n" +
                "---\n" +
                "\n" +
                buildNewCasesIncreaseOverActivesRow() +
                "\n" +
                "---\n" +
                "\n" +
                "## Média Móvel a 7 dias\n" +
                "\n" +
                "|**Novos**|" +
                buildMovingAverageDaysHeader() +
                ":--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|\n" +
                "|**\uD83D\uDC65**|" +
                buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getCases() - stat1.getCases())) +
                "|**✔️**|" +
                buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getRecoveries() - stat1.getRecoveries())) +
                "|**☠️**|" +
                buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getDeaths() - stat1.getDeaths())) +
                "|**\uD83C\uDFE5**|" +
                buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getHospitalized() - stat1.getHospitalized())) +
                "|**\uD83D\uDECC**|" +
                buildStatMovingAverageRow((stat1, stat2) -> (double) (stat2.getIcu() - stat1.getIcu())) +
                "\n" +
                "---\n" +
                "\n" +
                "## Variação de novos casos confirmados em relação ao mesmo dia de semanas anteriores\n" +
                "\n" +
                "||**1 sem**|**2 sem**|**3 sem**|**4 sem**|\n" +
                ":--|--:|--:|--:|--:|\n" +
                "|**Norte**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.NORTE) +
                "|**Centro**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.CENTRO) +
                "|**LVT**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.LISBOA_E_VALE_DO_TEJO) +
                "|**Alentejo**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.ALENTEJO) +
                "|**Algarve**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.ALGARVE) +
                "|**Açores**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.ACORES) +
                "|**Madeira**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.MADEIRA) +
                "|**Geral**|" +
                buildMultiWeekCaseVariationRow(GeographicZone.COUNTRYWIDE) +
                "\n" +
                "---\n" +
                "\n" +
                "## Por região\n" +
                "\n" +
                "**Casos Confirmados**\n" +
                "\n" +
                "|**Região**|**\uD83D\uDC65 Totais**|**Variação**|**\uD83D\uDCC8 1 dia**|**\uD83D\uDCC8 3 dias**|**\uD83D\uDCC8 5 dias**|**\uD83D\uDCC8 7 dias**|\n" +
                ":--|--:|--:|--:|--:|--:|--:|--:|\n" +
                "|**Norte**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getNorthCases(),
                        (stat1, stat2) -> stat2.getNorthCases() - stat1.getNorthCases(),
                        (stat1, stat2) -> stat2.getNorthCases() / (double) stat1.getNorthCases()) +
                "|**Centro**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getCenterCases(),
                        (stat1, stat2) -> stat2.getCenterCases() - stat1.getCenterCases(),
                        (stat1, stat2) -> stat2.getCenterCases() / (double) stat1.getCenterCases()) +
                "|**LVT**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getLvtCases(),
                        (stat1, stat2) -> stat2.getLvtCases() - stat1.getLvtCases(),
                        (stat1, stat2) -> stat2.getLvtCases() / (double) stat1.getLvtCases()) +
                "|**Alentejo**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAlentejoCases(),
                        (stat1, stat2) -> stat2.getAlentejoCases() - stat1.getAlentejoCases(),
                        (stat1, stat2) -> stat2.getAlentejoCases() / (double) stat1.getAlentejoCases()) +
                "|**Algarve**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAlgarveCases(),
                        (stat1, stat2) -> stat2.getAlgarveCases() - stat1.getAlgarveCases(),
                        (stat1, stat2) -> stat2.getAlgarveCases() / (double) stat1.getAlgarveCases()) +
                "|**Açores**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAzoresCases(),
                        (stat1, stat2) -> stat2.getAzoresCases() - stat1.getAzoresCases(),
                        (stat1, stat2) -> stat2.getAzoresCases() / (double) stat1.getAzoresCases()) +
                "|**Madeira**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getMadeiraCases(),
                        (stat1, stat2) -> stat2.getMadeiraCases() - stat1.getMadeiraCases(),
                        (stat1, stat2) -> stat2.getMadeiraCases() / (double) stat1.getMadeiraCases()) +
                "\n" +
                "**Óbitos**\n" +
                "\n" +
                "|**Região**|**☠️ Óbitos**|**Variação**|**\uD83D\uDCC8 1 dia**|**\uD83D\uDCC8 3 dias**|**\uD83D\uDCC8 5 dias**|\uD83D\uDCC8 7 dias|\n" +
                ":--|--:|--:|--:|--:|--:|--:|\n" +
                "|**Norte**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getNorthDeaths(),
                        (stat1, stat2) -> stat2.getNorthDeaths() - stat1.getNorthDeaths(),
                        (stat1, stat2) -> stat2.getNorthDeaths() / (double) stat1.getNorthDeaths()) +
                "|**Centro**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getCenterDeaths(),
                        (stat1, stat2) -> stat2.getCenterDeaths() - stat1.getCenterDeaths(),
                        (stat1, stat2) -> stat2.getCenterDeaths() / (double) stat1.getCenterDeaths()) +
                "|**LVT**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getLvtDeaths(),
                        (stat1, stat2) -> stat2.getLvtDeaths() - stat1.getLvtDeaths(),
                        (stat1, stat2) -> stat2.getLvtDeaths() / (double) stat1.getLvtDeaths()) +
                "|**Alentejo**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAlentejoDeaths(),
                        (stat1, stat2) -> stat2.getAlentejoDeaths() - stat1.getAlentejoDeaths(),
                        (stat1, stat2) -> stat2.getAlentejoDeaths() / (double) stat1.getAlentejoDeaths()) +
                "|**Algarve**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAlgarveDeaths(),
                        (stat1, stat2) -> stat2.getAlgarveDeaths() - stat1.getAlgarveDeaths(),
                        (stat1, stat2) -> stat2.getAlgarveDeaths() / (double) stat1.getAlgarveDeaths()) +
                "|**Açores**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getAzoresDeaths(),
                        (stat1, stat2) -> stat2.getAzoresDeaths() - stat1.getAzoresDeaths(),
                        (stat1, stat2) -> stat2.getAzoresDeaths() / (double) stat1.getAzoresDeaths()) +
                "|**Madeira**|" +
                buildStatsRow(sevenDayCovidData.get(tableDate).getMadeiraDeaths(),
                        (stat1, stat2) -> stat2.getMadeiraDeaths() - stat1.getMadeiraDeaths(),
                        (stat1, stat2) -> stat2.getMadeiraDeaths() / (double) stat1.getMadeiraDeaths()) +
                "\n" +
                "**Proporção de novos casos e de óbitos por ARS**\n" +
                "\n" +
                "||**Norte**|**Centro**|**LVT**|**Alentejo**|**Algarve**|**Açores**|**Madeira**|\n" +
                ":--:|:--:|:--:|:--:|:--:|:--:|:--:|:--:|\n" +
                "|Novos Casos|" +
                buildRegionsInfluencePercentOnGlobalStatRow(StatType.CASES) +
                "|Novos Óbitos|" +
                buildRegionsInfluencePercentOnGlobalStatRow(StatType.DEATHS) +
                "\n" +
                "**Dados obtidos automaticamente do *site* do [Ministério da Saúde dedicado à COVID-19](https://covid19.min-saude.pt/relatorio-de-situacao/).**\n" +
                "\n" +
                "*Script* original por BarcaDoInferno e hsamtronp.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String endpoint() {
        return "reddit";
    }
}
