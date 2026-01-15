package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PartialRegexRegisteredServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
class PartialRegexRegisteredServiceMatchingStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        service.setMatchingStrategy(strategy);
        MAPPER.writeValue(jsonFile, service);
        val read = MAPPER.readValue(jsonFile, RegisteredService.class);
        assertEquals(read, service);
    }

    @Test
    void verifyPattern1() {
        val service = RegisteredServiceTestUtils.getRegisteredService("https://.*");
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL));
        assertTrue(strategy.matches(service, "^https://"));
    }

    @Test
    void verifyPattern2() {
        val service = RegisteredServiceTestUtils.getRegisteredService("\\d\\d\\d");
        val strategy = new PartialRegexRegisteredServiceMatchingStrategy();
        assertTrue(strategy.matches(service, "https://google123.com"));
        assertTrue(strategy.matches(service, "https://google.com?param1=value123here"));
    }

}
