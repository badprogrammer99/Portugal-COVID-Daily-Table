package com.gmail.etpr99.jose.coviddailytable.tables;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.models.CovidStats;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusDays;
import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.minusWeeks;

public abstract class AbstractCovidDataTableBuilder implements CovidDataTableBuilder {
    protected CovidDataSource covidDataSource;
    protected Date tableDate;
    protected Date datePreviousToTableDate;
    protected Map<Date, CovidStats> sevenDayCovidData;

    protected int casesInTableDate;
    protected int recoveriesInTableDate;
    protected int deathsInTableDate;
    protected int hospitalizedInTableDate;
    protected int icuInTableDate;
    protected int activeInTableDate;

    protected int dayOfDate;
    protected int monthOfDate;
    protected int yearOfDate;

    private Calendar cal;

    public AbstractCovidDataTableBuilder(CovidDataSource covidDataSource, Date tableDate) {
        this.covidDataSource = covidDataSource;
        this.tableDate = tableDate;
        datePreviousToTableDate = minusDays(tableDate, 1);
        try {
            sevenDayCovidData = covidDataSource.getCovidDataInbetweenDays(minusWeeks(tableDate, 1), tableDate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        casesInTableDate = sevenDayCovidData.get(tableDate).getCases();
        recoveriesInTableDate = sevenDayCovidData.get(tableDate).getRecoveries();
        deathsInTableDate = sevenDayCovidData.get(tableDate).getDeaths();
        hospitalizedInTableDate = sevenDayCovidData.get(tableDate).getHospitalized();
        icuInTableDate = sevenDayCovidData.get(tableDate).getIcu();
        activeInTableDate = sevenDayCovidData.get(tableDate).getActive();

        cal = Calendar.getInstance();
        cal.setTime(tableDate);

        dayOfDate = cal.get(Calendar.DAY_OF_MONTH);
        monthOfDate = cal.get(Calendar.MONTH) + 1;
        yearOfDate = cal.get(Calendar.YEAR);
    }

    protected String printMonthNameInPortuguese() {
        return Month.of(cal.get(Calendar.MONTH) + 1).getDisplayName(TextStyle.FULL_STANDALONE, new Locale("pt"));
    }
}
