package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OpenPolicyAgentRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
public class OpenPolicyAgentRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "OpenPolicyAgentRegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeToJson() throws IOException {
        val strategyWritten = new OpenPolicyAgentRegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setDecision("example/authz/allow");
        strategyWritten.setToken(UUID.randomUUID().toString());
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val read = MAPPER.readValue(JSON_FILE, OpenPolicyAgentRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    public void verifyOperation() throws Exception {
        val strategy = new OpenPolicyAgentRegisteredServiceAccessStrategy();
        strategy.setApiUrl("http://localhost:8755");
        strategy.setDecision("example/authz/allow");
        strategy.setToken(UUID.randomUUID().toString());
        strategy.setContext(Map.of("Param1", List.of("Value1")));

        val principalAttributes = new HashMap<String, List<Object>>();
        principalAttributes.put("email", List.of("user@example.org"));
        val principal = RegisteredServiceTestUtils.getPrincipal("person", principalAttributes);
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService())
            .principalId(principal.getId())
            .attributes(principal.getAttributes())
            .build();
        assertFalse(strategy.doPrincipalAttributesAllowServiceAccess(request));

        val mapper = JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();
        val data = mapper.writeValueAsString(CollectionUtils.wrap("result", true));
        try (val webServer = new MockWebServer(8755,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.doPrincipalAttributesAllowServiceAccess(request));
        }
    }

}
