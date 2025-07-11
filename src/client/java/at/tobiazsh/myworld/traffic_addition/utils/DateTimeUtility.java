package at.tobiazsh.myworld.traffic_addition.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtility {

    private static final DateTimeFormatter militaryTimeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Fuck 12h time lol
    private static final DateTimeFormatter ordinalFormatter = DateTimeFormatter.ofPattern("d'${suffix}' MMMM yyyy");

    public static String formatWithOrdinal(ZonedDateTime zonedDateTime) {
        int day = zonedDateTime.getDayOfMonth();
        String suffix = getDaySuffix(day);

        return ordinalFormatter.format(zonedDateTime).replace("${suffix}", suffix);
    }

    public static String formatMilitaryTime(ZonedDateTime zonedDateTime) {
        return militaryTimeFormatter.format(zonedDateTime);
    }

    public static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1 -> { return "st"; }
            case 2 -> { return "nd"; }
            case 3 -> { return "rd"; }
            default -> { return "th"; }
        }
    }

}
