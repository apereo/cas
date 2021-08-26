package org.apereo.cas.util.spring;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ConvertersTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class ConvertersTests {
    @Test
    public void verifyOperation() {
        assertNotNull(new Converters.ZonedDateTimeToStringConverter().convert(ZonedDateTime.now(Clock.systemUTC())));
    }
}
