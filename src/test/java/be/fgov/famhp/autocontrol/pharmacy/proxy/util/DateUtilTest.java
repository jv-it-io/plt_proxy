package be.fgov.famhp.autocontrol.pharmacy.proxy.util;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateUtilTest {

    @Test
    public void toLocalDate() {
        DateTime dateTime = new DateTime();
        LocalDate localDate = DateUtil.toLocalDate(dateTime);
        assertEquals(dateTime.getYear(), localDate.getYear());
        assertEquals(dateTime.getMonthOfYear(), localDate.getMonth().getValue());
        assertEquals(dateTime.getDayOfMonth(), localDate.getDayOfMonth());
    }

}
