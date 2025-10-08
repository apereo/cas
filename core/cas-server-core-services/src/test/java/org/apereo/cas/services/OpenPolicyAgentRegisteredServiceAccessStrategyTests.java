package org.apereo.cas.services;

import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class OpenPolicyAgentRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new OpenPolicyAgentRegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setDecision("example/authz/allow");
        strategyWritten.setToken(UUID.randomUUID().toString());
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, OpenPolicyAgentRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    void verifyOperation() throws Throwable {
        val strategy = new OpenPolicyAgentRegisteredServiceAccessStrategy();
        strategy.setApiUrl("http://localhost:8756");
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
            .applicationContext(applicationContext)
            .build();
        assertFalse(strategy.authorizeRequest(request));

        val mapper = JacksonObjectMapperFactory.builder().defaultTypingEnabled(false).build().toObjectMapper();
        val data = mapper.writeValueAsString(CollectionUtils.wrap("result", true));
        try (val webServer = new MockWebServer(8756,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.authorizeRequest(request));
        }
    }

}
