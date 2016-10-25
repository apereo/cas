package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.Assert.*;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategyTests} is responsible for
 * running test cases for {@link TimeBasedRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TimeBasedRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File("timeBasedRegisteredServiceAccessStrategy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void checkAuthorizationByRangePass() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertTrue(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangeFailStartTime() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertFalse(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangePassEndTime() {
        final TimeBasedRegisteredServiceAccessStrategy authz =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30).toString());
        assertTrue(authz.isServiceAccessAllowed());
    }

    @Test
    public void verifySerializeATimeBasedRegisteredServiceAccessStrategyToJson() throws IOException {
        final TimeBasedRegisteredServiceAccessStrategy authWritten =
                new TimeBasedRegisteredServiceAccessStrategy(true, true);

        mapper.writeValue(JSON_FILE, authWritten);

        final RegisteredServiceAccessStrategy credentialRead = mapper.readValue(JSON_FILE, TimeBasedRegisteredServiceAccessStrategy.class);

        assertEquals(authWritten, credentialRead);
    }
}
