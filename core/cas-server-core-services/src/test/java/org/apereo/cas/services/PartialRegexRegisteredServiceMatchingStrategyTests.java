package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PartialRegexRegisteredServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class PartialRegexRegisteredServiceMatchingStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "PartialRegexRegisteredServiceMatchingStrategyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        service.setMatchingStrategy(strategy);
        MAPPER.writeValue(JSON_FILE, service);
        val read = MAPPER.readValue(JSON_FILE, RegisteredService.class);
        assertEquals(read, service);
    }

    @Test
    void verifyPattern1() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("https://.*");
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL));
        assertTrue(strategy.matches(service, "^https://"));
    }

    @Test
    void verifyPattern2() throws Throwable {
        val service = RegisteredServiceTestUtils.getRegisteredService("\\d\\d\\d");
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        assertTrue(strategy.matches(service, "https://google123.com"));
        assertTrue(strategy.matches(service, "https://google.com?param1=value123here"));
    }

}
