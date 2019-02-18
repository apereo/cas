package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link TimeBasedRegisteredServiceAccessStrategyTests} is responsible for
 * running test cases for {@link TimeBasedRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TimeBasedRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "timeBasedRegisteredServiceAccessStrategy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void checkAuthorizationByRangePass() {
        val authz =
            new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertTrue(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangeFailStartTime() {
        val authz =
            new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertFalse(authz.isServiceAccessAllowed());

    }

    @Test
    public void checkAuthorizationByRangePassEndTime() {
        val authz =
            new TimeBasedRegisteredServiceAccessStrategy(true, true);
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30).toString());
        assertTrue(authz.isServiceAccessAllowed());
    }

    @Test
    public void verifySerializeATimeBasedRegisteredServiceAccessStrategyToJson() throws IOException {
        val authWritten = new TimeBasedRegisteredServiceAccessStrategy(true, true);
        MAPPER.writeValue(JSON_FILE, authWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, TimeBasedRegisteredServiceAccessStrategy.class);
        assertEquals(authWritten, credentialRead);
    }
}
