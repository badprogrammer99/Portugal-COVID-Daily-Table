package com.gmail.etpr99.jose.coviddailytable.tablebuilders;

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

    protected int dayOfDate;
    protected int monthOfDate;
    protected int yearOfDate;

    protected Map<Date, CovidStats> sevenDayCovidData;

    protected int casesInTableDate;
    protected int recoveriesInTableDate;
    protected int deathsInTableDate;
    protected int hospitalizedInTableDate;
    protected int icuInTableDate;
    protected int activeInTableDate;

    private final Calendar cal = Calendar.getInstance();

    public AbstractCovidDataTableBuilder(CovidDataSource covidDataSource) {
        this.covidDataSource = covidDataSource;
    }

    @Override
    public Date getTableDate() {
        return tableDate;
    }

    @Override
    public void setTableDate(Date tableDate) {
        this.tableDate = tableDate;
        datePreviousToTableDate = minusDays(tableDate, 1);

        cal.setTime(tableDate);
        dayOfDate = cal.get(Calendar.DAY_OF_MONTH);
        monthOfDate = cal.get(Calendar.MONTH) + 1;
        yearOfDate = cal.get(Calendar.YEAR);

        sevenDayCovidData = covidDataSource.getCovidDataInbetweenDays(minusWeeks(tableDate, 1), tableDate);

        casesInTableDate = sevenDayCovidData.get(this.tableDate).getCases();
        recoveriesInTableDate = sevenDayCovidData.get(this.tableDate).getRecoveries();
        deathsInTableDate = sevenDayCovidData.get(this.tableDate).getDeaths();
        hospitalizedInTableDate = sevenDayCovidData.get(this.tableDate).getHospitalized();
        icuInTableDate = sevenDayCovidData.get(this.tableDate).getIcu();
        activeInTableDate = sevenDayCovidData.get(this.tableDate).getActive();
    }

    protected String printMonthNameInPortuguese() {
        return Month.of(cal.get(Calendar.MONTH) + 1).getDisplayName(TextStyle.FULL_STANDALONE, new Locale("pt"));
    }
}
