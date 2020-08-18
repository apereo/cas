package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LiteralRegisteredServiceMatchingStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("RegisteredService")
public class LiteralRegisteredServiceMatchingStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ExactLiteralRegisteredServiceMatchingStrategyTests.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules()
        .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

    @Test
    public void verifySerialization() throws Exception {
        val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = new LiteralRegisteredServiceMatchingStrategy().setCaseInsensitive(true);
        service.setMatchingStrategy(strategy);
        MAPPER.writeValue(JSON_FILE, service);
        val read = MAPPER.readValue(JSON_FILE, RegisteredService.class);
        assertEquals(read, service);
    }

    @Test
    public void verifyOperationCaseInsensitive() {
        val service = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
        val strategy = new LiteralRegisteredServiceMatchingStrategy().setCaseInsensitive(true);
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL));
        assertTrue(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL.toUpperCase()));
        assertFalse(strategy.matches(service, "https://.*"));
    }

    @Test
    public void verifyOperationCaseSensitive() {
        val service = RegisteredServiceTestUtils.getRegisteredService(RegisteredServiceTestUtils.CONST_TEST_URL);
        val strategy = new LiteralRegisteredServiceMatchingStrategy().setCaseInsensitive(false);
        assertFalse(strategy.matches(service, RegisteredServiceTestUtils.CONST_TEST_URL.toUpperCase()));
    }


}
