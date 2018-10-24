package org.apereo.cas.util;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * This is {@link DateTimeUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DateTimeUtilsTests {

    @Test
    public void verifyParsingDateAsLocalDateTime() {
        assertNotNull(DateTimeUtils.localDateTimeOf(LocalDateTime.now().toString()));
    }

    @Test
    public void verifyParsingDateAsLocalDate() {
        assertNotNull(DateTimeUtils.localDateTimeOf(LocalDate.now().toString()));
    }

    @Test
    public void verifyParsingDateAsLocalDateString1() {
        assertNotNull(DateTimeUtils.localDateTimeOf("2017-10-15"));
    }

    @Test
    public void verifyParsingDateAsLocalDateString2() {
        assertNotNull(DateTimeUtils.localDateTimeOf("09/19/2017"));
    }

    @Test
    public void verifyParsingDateAsLocalDateString3() {
        assertNotNull(DateTimeUtils.localDateTimeOf("09/19/2017 4:30 pm"));
    }

    @Test
    public void verifyParsingDateAsLocalDateString4() {
        assertNotNull(DateTimeUtils.localDateTimeOf("2017-10-12T07:00:00.000Z"));
    }

    @Test
    public void verifyParsingCalendar() {
        val calendar = Calendar.getInstance();
        assertNotNull(DateTimeUtils.zonedDateTimeOf(calendar));
    }

    @Test
    public void verifyParsingChronoUnit() {
        assertEquals(ChronoUnit.DAYS, DateTimeUtils.toChronoUnit(TimeUnit.DAYS));
        assertEquals(ChronoUnit.HOURS, DateTimeUtils.toChronoUnit(TimeUnit.HOURS));
        assertEquals(ChronoUnit.MINUTES, DateTimeUtils.toChronoUnit(TimeUnit.MINUTES));
        assertEquals(ChronoUnit.SECONDS, DateTimeUtils.toChronoUnit(TimeUnit.SECONDS));
        assertEquals(ChronoUnit.MICROS, DateTimeUtils.toChronoUnit(TimeUnit.MICROSECONDS));
        assertEquals(ChronoUnit.MILLIS, DateTimeUtils.toChronoUnit(TimeUnit.MILLISECONDS));
        assertEquals(ChronoUnit.NANOS, DateTimeUtils.toChronoUnit(TimeUnit.NANOSECONDS));
    }
}
