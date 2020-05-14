package org.apereo.cas.util;

import lombok.val;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DateTimeUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Tag("Simple")
public class DateTimeUtilsTests {

    @Test
    public void verifyParsingDateAsLocalDateTime() {
        assertNotNull(DateTimeUtils.localDateTimeOf(LocalDateTime.now(ZoneId.systemDefault()).toString()));
    }

    @Test
    public void verifyParsingDateAsLocalDate() {
        assertNotNull(DateTimeUtils.localDateTimeOf(LocalDateTime.now(ZoneId.systemDefault()).toString()));
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
    @SuppressWarnings({"PreferJavaTimeOverload", "JavaTimeDefaultTimeZone"})
    public void verifyConvert() {
        assertNotNull(DateTimeUtils.convertToZonedDateTime(LocalDateTime.now().toString()));
        assertNotNull(DateTimeUtils.convertToZonedDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString()));
        assertNotNull(DateTimeUtils.zonedDateTimeOf(DateTime.now().toInstant()));
        assertNotNull(DateTimeUtils.zonedDateTimeOf(System.currentTimeMillis()));
        assertNotNull(DateTimeUtils.localDateTimeOf(new Date()));
        assertNotNull(DateTimeUtils.localDateTimeOf(System.currentTimeMillis()));
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

    @Test
    public void verifyTimeUnit() {
        assertEquals(TimeUnit.DAYS, DateTimeUtils.toTimeUnit(ChronoUnit.DAYS));
        assertEquals(TimeUnit.HOURS, DateTimeUtils.toTimeUnit(ChronoUnit.HOURS));
        assertEquals(TimeUnit.MINUTES, DateTimeUtils.toTimeUnit(ChronoUnit.MINUTES));
        assertEquals(TimeUnit.SECONDS, DateTimeUtils.toTimeUnit(ChronoUnit.SECONDS));
        assertEquals(TimeUnit.MICROSECONDS, DateTimeUtils.toTimeUnit(ChronoUnit.MICROS));
        assertEquals(TimeUnit.MILLISECONDS, DateTimeUtils.toTimeUnit(ChronoUnit.MILLIS));
        assertEquals(TimeUnit.NANOSECONDS, DateTimeUtils.toTimeUnit(ChronoUnit.NANOS));
        assertThrows(UnsupportedOperationException.class, () -> assertNotNull(DateTimeUtils.toTimeUnit(ChronoUnit.WEEKS)));
    }
}
