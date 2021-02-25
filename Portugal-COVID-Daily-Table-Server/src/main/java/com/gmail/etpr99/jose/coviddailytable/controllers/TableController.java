package com.gmail.etpr99.jose.coviddailytable.controllers;

import com.gmail.etpr99.jose.coviddailytable.exceptions.TodayReportNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.tablebuilders.CovidDataTableBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.gmail.etpr99.jose.coviddailytable.utils.DateUtils.*;

@Controller
@RequestMapping("/tables")
public class TableController {

    @Autowired
    @Qualifier("covidDataTableBuilderSet")
    private Set<CovidDataTableBuilder> covidDataTableBuilderSet;

    @GetMapping("/{tableType}")
    @ResponseBody
    public String buildTable(HttpServletResponse response,
                             @PathVariable("tableType") String tableType,
                             @RequestParam("date") Optional<String> date) {
        CovidDataTableBuilder tableBuilder = covidDataTableBuilderSet.stream()
                .filter(tb -> tb.endpoint().equalsIgnoreCase(tableType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not find a table builder for the specified table type."));

        if (date.isPresent()) {
            DateFormat dateFormatter = new SimpleDateFormat(Objects.requireNonNull(determineDateFormat(date.get())));
            try {
                tableBuilder.setTableDate(dateWithoutTime(dateFormatter.parse(date.get())));
            } catch (ParseException pe) {
                throw new RuntimeException(pe);
            }
        } else {
            try {
                tableBuilder.setTableDate(dateWithoutTime(new Date()));
            } catch (TodayReportNonExistentException trnee) {
                tableBuilder.setTableDate(dateWithoutTime(minusDays(1)));
            }
        }

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        return tableBuilder.buildTable();
    }
}
