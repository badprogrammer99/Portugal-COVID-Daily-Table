package com.gmail.etpr99.jose.coviddailytable;

import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSource;
import com.gmail.etpr99.jose.coviddailytable.datasources.CovidDataSourceImpl;
import com.gmail.etpr99.jose.coviddailytable.tables.HTMLCovidDataTableBuilder;
import com.gmail.etpr99.jose.coviddailytable.tables.CovidDataTableBuilder;
import com.gmail.etpr99.jose.coviddailytable.tables.RedditCovidDataTableBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.*;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) throw new RuntimeException("No arguments were passed.");

        CovidDataSource covidDataSource = new CovidDataSourceImpl();

        Date dateToRetrieveDataFor;
        CovidDataTableBuilder covidDataTableBuilder;

        try {
            dateToRetrieveDataFor = new SimpleDateFormat(Objects.requireNonNull(determineDateFormat(args[0]))).parse(args[0]);
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not recognize a valid date format from passed date");
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }

        if (args[1].equalsIgnoreCase("html")) {
            covidDataTableBuilder = new HTMLCovidDataTableBuilder(covidDataSource, dateToRetrieveDataFor);
        } else if (args[1].equalsIgnoreCase("reddit")) {
            covidDataTableBuilder = new RedditCovidDataTableBuilder(covidDataSource, dateToRetrieveDataFor);
        } else {
            throw new RuntimeException("Unknown covid data table builder type");
        }

        System.out.println(covidDataTableBuilder.buildTable());
    }
}
