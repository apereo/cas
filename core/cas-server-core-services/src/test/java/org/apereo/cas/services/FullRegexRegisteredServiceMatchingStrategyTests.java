package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FullRegexRegisteredServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class FullRegexRegisteredServiceMatchingStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = new FullRegexRegisteredServiceMatchingStrategy();
        service.setMatchingStrategy(strategy);
        MAPPER.writeValue(jsonFile, service);
        val read = MAPPER.readValue(jsonFile, RegisteredService.class);
        assertEquals(read, service);
    }

    @Test
    void verifyOperation() {
        val service = RegisteredServiceTestUtils.getRegisteredService("https://.*");
        val strategy = new FullRegexRegisteredServiceMatchingStrategy();
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL));
        assertFalse(strategy.matches(service, "https"));
    }

    @Test
    void verifyPattern2() {
        val service = RegisteredServiceTestUtils.getRegisteredService("\\d\\d\\d");
        val strategy = new FullRegexRegisteredServiceMatchingStrategy();
        assertFalse(strategy.matches(service, "https://google123.com"));
    }

}
