package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDateTime;
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
@Tag("RegisteredService")
class TimeBasedRegisteredServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "timeBasedRegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void checkAuthorizationByRangePass() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));

    }

    @Test
    void checkAuthorizationByRangeFailStartTime() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusDays(1).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10).toString());
        assertFalse(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));

    }

    @Test
    void checkFailWithNowAfterEndTime() {
        var authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(10).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).minusDays(5).toString());
        assertFalse(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void checkLocalFailWithNowAfterEndTime() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(LocalDateTime.now(ZoneOffset.UTC).minusDays(10).toString());
        authz.setEndingDateTime(LocalDateTime.now(ZoneOffset.UTC).minusDays(5).toString());
        assertFalse(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void checkLocalFailWithNowBeforeStartTime() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(10).toString());
        authz.setEndingDateTime(LocalDateTime.now(ZoneOffset.UTC).minusDays(15).toString());
        assertFalse(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void checkExpressionLanguage() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime("${#localStartDay}");
        authz.setEndingDateTime("${#localEndDay}");
        authz.setZoneId("${#zoneId}");
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void checkAuthorizationByRangePassEndTime() {
        val authz = new TimeBasedRegisteredServiceAccessStrategy();
        authz.setStartingDateTime(ZonedDateTime.now(ZoneOffset.UTC).toString());
        authz.setEndingDateTime(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(30).toString());
        assertTrue(authz.isServiceAccessAllowed(RegisteredServiceTestUtils.getRegisteredService(), CoreAuthenticationTestUtils.getService()));
    }

    @Test
    void verifySerializeATimeBasedRegisteredServiceAccessStrategyToJson() {
        val authWritten = new TimeBasedRegisteredServiceAccessStrategy();
        MAPPER.writeValue(JSON_FILE, authWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, TimeBasedRegisteredServiceAccessStrategy.class);
        assertEquals(authWritten, credentialRead);
    }
}
