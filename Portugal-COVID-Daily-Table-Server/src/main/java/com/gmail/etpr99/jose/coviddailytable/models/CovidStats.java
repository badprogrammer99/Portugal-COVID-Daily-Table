package com.gmail.etpr99.jose.coviddailytable.models;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Class that represents COVID data stats registered in a certain day.
 */
public class CovidStats {

    /**
     * How many cases were registered.
     */
    @CsvBindByName(column = "confirmados")
    private int cases;

    /**
     * How many recoveries were registered.
     */
    @CsvBindByName(column = "recuperados")
    private int recoveries;

    /**
     * How many deaths were registered.
     */
    @CsvBindByName(column = "obitos")
    private int deaths;

    /**
     * How many hospitalized people were registered (can be a negative number).
     */
    @CsvBindByName(column = "internados")
    private int hospitalized;

    /**
     * How many hospitalized people in ICU units were registered (can be a negative number).
     */
    @CsvBindByName(column = "internados_uci")
    private int icu;

    /**
     * How many active cases were registered (can be a negative number).
     */
    @CsvBindByName(column = "ativos")
    private int active;

    @CsvBindByName(column = "confirmados_arsnorte")
    private int northCases;

    @CsvBindByName(column = "obitos_arsnorte")
    private int northDeaths;

    @CsvBindByName(column = "confirmados_arscentro")
    private int centerCases;

    @CsvBindByName(column = "obitos_arscentro")
    private int centerDeaths;

    @CsvBindByName(column = "confirmados_arslvt")
    private int lvtCases;

    @CsvBindByName(column = "obitos_arslvt")
    private int lvtDeaths;

    @CsvBindByName(column = "confirmados_arsalentejo")
    private int alentejoCases;

    @CsvBindByName(column = "obitos_arsalentejo")
    private int alentejoDeaths;

    @CsvBindByName(column = "confirmados_arsalgarve")
    private int algarveCases;

    @CsvBindByName(column = "obitos_arsalgarve")
    private int algarveDeaths;

    @CsvBindByName(column = "confirmados_acores")
    private int azoresCases;

    @CsvBindByName(column = "obitos_acores")
    private int azoresDeaths;

    @CsvBindByName(column = "confirmados_madeira")
    private int madeiraCases;

    @CsvBindByName(column = "obitos_madeira")
    private int madeiraDeaths;

    /**
     * The date where this data was registered.
     */
    @CsvBindByName(column = "data")
    @CsvDate("dd-MM-yyyy")
    private Date date;

    public CovidStats() { }

    public CovidStats(int cases, int recoveries, int deaths, int hospitalized, int icu, int active, Date day,
                      int northCases, int northDeaths, int centerCases, int centerDeaths, int lvtCases, int lvtDeaths,
                      int alentejoCases, int alentejoDeaths, int algarveCases, int algarveDeaths, int azoresCases,
                      int azoresDeaths, int madeiraCases, int madeiraDeaths) {
        this.cases = cases;
        this.recoveries = recoveries;
        this.deaths = deaths;
        this.hospitalized = hospitalized;
        this.icu = icu;
        this.active = active;
        this.date = day;
        this.northCases = northCases;
        this.northDeaths = northDeaths;
        this.centerCases = centerCases;
        this.centerDeaths = centerDeaths;
        this.lvtCases = lvtCases;
        this.lvtDeaths = lvtDeaths;
        this.alentejoCases = alentejoCases;
        this.alentejoDeaths = alentejoDeaths;
        this.algarveCases = algarveCases;
        this.algarveDeaths = algarveDeaths;
        this.azoresCases = azoresCases;
        this.azoresDeaths = azoresDeaths;
        this.madeiraCases = madeiraCases;
        this.madeiraDeaths = madeiraDeaths;
    }

    public int getCases() {
        return cases;
    }

    public void setCases(int cases) {
        this.cases = cases;
    }

    public int getRecoveries() {
        return recoveries;
    }

    public void setRecoveries(int recoveries) {
        this.recoveries = recoveries;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getHospitalized() {
        return hospitalized;
    }

    public void setHospitalized(int hospitalized) {
        this.hospitalized = hospitalized;
    }

    public int getIcu() {
        return icu;
    }

    public void setIcu(int icu) {
        this.icu = icu;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getNorthCases() {
        return northCases;
    }

    public void setNorthCases(int northCases) {
        this.northCases = northCases;
    }

    public int getNorthDeaths() {
        return northDeaths;
    }

    public void setNorthDeaths(int northDeaths) {
        this.northDeaths = northDeaths;
    }

    public int getCenterCases() {
        return centerCases;
    }

    public void setCenterCases(int centerCases) {
        this.centerCases = centerCases;
    }

    public int getCenterDeaths() {
        return centerDeaths;
    }

    public void setCenterDeaths(int centerDeaths) {
        this.centerDeaths = centerDeaths;
    }

    public int getLvtCases() {
        return lvtCases;
    }

    public void setLvtCases(int lvtCases) {
        this.lvtCases = lvtCases;
    }

    public int getLvtDeaths() {
        return lvtDeaths;
    }

    public void setLvtDeaths(int lvtDeaths) {
        this.lvtDeaths = lvtDeaths;
    }

    public int getAlentejoCases() {
        return alentejoCases;
    }

    public void setAlentejoCases(int alentejoCases) {
        this.alentejoCases = alentejoCases;
    }

    public int getAlentejoDeaths() {
        return alentejoDeaths;
    }

    public void setAlentejoDeaths(int alentejoDeaths) {
        this.alentejoDeaths = alentejoDeaths;
    }

    public int getAlgarveCases() {
        return algarveCases;
    }

    public void setAlgarveCases(int algarveCases) {
        this.algarveCases = algarveCases;
    }

    public int getAlgarveDeaths() {
        return algarveDeaths;
    }

    public void setAlgarveDeaths(int algarveDeaths) {
        this.algarveDeaths = algarveDeaths;
    }

    public int getAzoresCases() {
        return azoresCases;
    }

    public void setAzoresCases(int azoresCases) {
        this.azoresCases = azoresCases;
    }

    public int getAzoresDeaths() {
        return azoresDeaths;
    }

    public void setAzoresDeaths(int azoresDeaths) {
        this.azoresDeaths = azoresDeaths;
    }

    public int getMadeiraCases() {
        return madeiraCases;
    }

    public void setMadeiraCases(int madeiraCases) {
        this.madeiraCases = madeiraCases;
    }

    public int getMadeiraDeaths() {
        return madeiraDeaths;
    }

    public void setMadeiraDeaths(int madeiraDeaths) {
        this.madeiraDeaths = madeiraDeaths;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "CovidStats{" +
                "cases=" + cases +
                ", recoveries=" + recoveries +
                ", deaths=" + deaths +
                ", hospitalized=" + hospitalized +
                ", icu=" + icu +
                ", active=" + active +
                ", northCases=" + northCases +
                ", northDeaths=" + northDeaths +
                ", centerCases=" + centerCases +
                ", centerDeaths=" + centerDeaths +
                ", lvtCases=" + lvtCases +
                ", lvtDeaths=" + lvtDeaths +
                ", alentejoCases=" + alentejoCases +
                ", alentejoDeaths=" + alentejoDeaths +
                ", algarveCases=" + algarveCases +
                ", algarveDeaths=" + algarveDeaths +
                ", azoresCases=" + azoresCases +
                ", azoresDeaths=" + azoresDeaths +
                ", madeiraCases=" + madeiraCases +
                ", madeiraDeaths=" + madeiraDeaths +
                ", day=" + date +
                '}';
    }

    public Map<GeographicZone, int[]> getRegionalCasesMap() {
        return new LinkedHashMap<>() {{
            put(GeographicZone.COUNTRYWIDE, new int[] { cases, deaths });
            put(GeographicZone.NORTE, new int[] { northCases, northDeaths });
            put(GeographicZone.CENTRO, new int[] { centerCases, centerDeaths });
            put(GeographicZone.LISBOA_E_VALE_DO_TEJO, new int[] { lvtCases, lvtDeaths });
            put(GeographicZone.ALENTEJO, new int[] { alentejoCases, alentejoDeaths });
            put(GeographicZone.ALGARVE, new int[] { algarveCases, algarveDeaths });
            put(GeographicZone.ACORES, new int[] { azoresCases, azoresDeaths });
            put(GeographicZone.MADEIRA, new int[] { madeiraCases, madeiraDeaths });
        }};
    }
}
