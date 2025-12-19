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
 * This is {@link StartsWithRegisteredServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("RegisteredService")
class StartsWithRegisteredServiceMatchingStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = new StartsWithRegisteredServiceMatchingStrategy().setExpectedUrl("https://google.com");
        service.setMatchingStrategy(strategy);
        MAPPER.writeValue(jsonFile, service);
        val read = MAPPER.readValue(jsonFile, RegisteredService.class);
        assertEquals(read, service);
    }

    @Test
    void verifyOperation() {
        val service = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
        val strategy = new StartsWithRegisteredServiceMatchingStrategy()
            .setExpectedUrl(RegisteredServiceTestUtils.CONST_TEST_URL);
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL));
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL.toUpperCase(Locale.ENGLISH)));
        assertFalse(strategy.matches(service, "https://.*"));
    }
}
