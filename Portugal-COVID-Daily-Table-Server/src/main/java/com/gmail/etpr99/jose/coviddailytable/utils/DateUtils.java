package com.gmail.etpr99.jose.coviddailytable.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {
    private static final Map<String, String> DATE_FORMAT_REGEXPS = new HashMap<>() {{
        put("^\\d{8}$", "yyyyMMdd");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}$", "MM/dd/yyyy");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}$", "yyyy/MM/dd");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
        put("^\\d{12}$", "yyyyMMddHHmm");
        put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$", "MM/dd/yyyy HH:mm");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
        put("^\\d{14}$", "yyyyMMddHHmmss");
        put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
        put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
        put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
        put("^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "MM/dd/yyyy HH:mm:ss");
        put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy/MM/dd HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
        put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
    }};

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public static String determineDateFormat(String dateString) {
        for (String regexp : DATE_FORMAT_REGEXPS.keySet()) {
            if (dateString.toLowerCase().matches(regexp)) {
                return DATE_FORMAT_REGEXPS.get(regexp);
            }
        }

        return null;
    }

    public static Date plusDays(int plusDay) {
        Date today = new Date();
        try {
            today = dateFormat.parse(dateFormat.format(today));
            return plusDays(today, plusDay);
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static Date plusDays(Date day, int plusDay) {
        try {
            return dateFormat.parse(dateFormat.format(Date.from(day.toInstant().plus(Duration.ofDays(plusDay)))));
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static Date minusDays(int minusDay) {
        Date today = new Date();
        try {
            today = dateFormat.parse(dateFormat.format(today));
            return minusDays(today, minusDay);
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static Date minusDays(Date day, int minusDay) {
        try {
            return dateFormat.parse(dateFormat.format(Date.from(day.toInstant().minus(Duration.ofDays(minusDay)))));
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static Date minusWeeks(int minusWeek) {
        Date today = new Date();
        try {
            today = dateFormat.parse(dateFormat.format(today));
        } catch (ParseException ignored) {
            return new Date();
        }

        return minusWeeks(today, minusWeek);
    }

    public static Date minusWeeks(Date day, int minusWeek) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(day);
        calendar.add(Calendar.WEEK_OF_YEAR, -minusWeek);
        Date newDate = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            return dateFormat.parse(dateFormat.format(Date.from(newDate.toInstant())));
        } catch (ParseException ignored) {
            return new Date();
        }
    }

    public static Date dateWithoutTime(Date date) {
        return minusDays(date, 0);
    }

    public static boolean areDaysEqual(Date date1, Date date2) {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        return dateFormat.format(date1).equals(dateFormat.format(date2));
    }

    public static long timeRemainingUntilTomorrow() {
        return plusDays(1).getTime() - new Date().getTime();
    }

    public static long convertNanoTimeToMillisecondTime(long nanos) {
        return nanos + (System.currentTimeMillis() - nanos);
    }
}
