package vn.aequitas.coreplatform.application.timesheet.helper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Timesheet naming helper. Port of the .NET {@code TimeHelper}; produces names
 * like {@code ITS_20260711}.
 */
public final class TimeHelper {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private TimeHelper() {
    }

    public static String generateTimesheetName(LocalDateTime date) {
        return "ITS_" + date.format(FORMAT);
    }
}
