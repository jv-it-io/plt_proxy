package be.fgov.famhp.autocontrol.pharmacy.proxy.util;

import org.joda.time.DateTime;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate toLocalDate(DateTime dateTime) {
        return LocalDate.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
    }
}
