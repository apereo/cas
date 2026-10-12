package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
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
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class RemoteEndpointServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new RemoteEndpointServiceAccessStrategy();
        MAPPER.writeValue(jsonFile, strategyWritten);
        val credentialRead = MAPPER.readValue(jsonFile, RemoteEndpointServiceAccessStrategy.class);
        assertEquals(strategyWritten, credentialRead);
    }

    @Test
    void verifyOperation() {
        val strategy = new RemoteEndpointServiceAccessStrategy();
        strategy.setAcceptableResponseCodes("200,201");
        try (val webServer = new MockWebServer(MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            strategy.setEndpointUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertTrue(strategy.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder()
                .applicationContext(applicationContext)
                .principalId("casuser").build()));
        }
    }

    @Test
    void verifyPayloadExcludesServiceDefinition() {
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("https://app.example.org.*");
        registeredService.setDescription("secret-description");
        registeredService.setProperties(Map.of("clientSecret",
            new DefaultRegisteredServiceProperty("secret-value")));
        val request = RegisteredServiceAccessStrategyRequest.builder()
            .applicationContext(applicationContext)
            .registeredService(registeredService)
            .service(RegisteredServiceTestUtils.getService("https://app.example.org/login"))
            .principalId("casuser")
            .attributes(Map.of("role", List.of("staff")))
            .build();
        val payload = RemoteEndpointServiceAccessStrategy.buildRequestPayload(request);
        assertEquals("casuser", payload.get("principalId"));
        assertEquals(Map.of("role", List.of("staff")), payload.get("attributes"));
        assertEquals(Map.of("id", "https://app.example.org/login"), payload.get("service"));
        val details = (Map<?, ?>) payload.get("registeredService");
        assertEquals(Set.of("id", "name", "serviceId", "friendlyName"), details.keySet());
        assertEquals(registeredService.getServiceId(), details.get("serviceId"));
        val json = JacksonObjectMapperFactory.builder().build().toObjectMapper().writeValueAsString(payload);
        assertFalse(json.contains("secret-value"));
        assertFalse(json.contains("secret-description"));
    }

    @Test
    void verifyFails() {
        val strategy = new RemoteEndpointServiceAccessStrategy();
        strategy.setEndpointUrl("http://localhost:1234");
        strategy.setAcceptableResponseCodes("600");
        try (val webServer = new MockWebServer(MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertFalse(strategy.authorizeRequest(RegisteredServiceAccessStrategyRequest.builder()
                .applicationContext(applicationContext)
                .principalId("casuser").build()));
        }
    }
}
