package org.apereo.cas.util;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
}
