package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CerbosRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
class CerbosRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new CerbosRegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setActions(List.of("read", "write", "view"));
        strategyWritten.setAuxData(Map.of("key", "value"));
        strategyWritten.setToken(UUID.randomUUID().toString());
        strategyWritten.setRolesAttribute("memberOf");
        strategyWritten.setScope("scope1");
        strategyWritten.setKind("kind1");
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, CerbosRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    void verifyOperation() {
        val requestId = UUID.randomUUID().toString();
        val strategy = new CerbosRegisteredServiceAccessStrategy();
        strategy.setApiUrl("http://localhost:3592");
        strategy.setActions(List.of("read", "view", "comment"));
        strategy.setKind("leave_request");
        strategy.setRequestId(requestId);
        strategy.setToken(UUID.randomUUID().toString());
        strategy.setRolesAttribute("memberOf");

        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService())
            .principalId("casuser")
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .attributes(CoreAuthenticationTestUtils.getAttributes())
            .applicationContext(applicationContext)
            .build();
        assertFalse(strategy.authorizeRequest(request));

        val data = """
            {
            "requestId": "%s",
            "results": [
                {
                "resource": {
                    "Id": "XX125",
                    "kind": "album:object"
                },
                "actions": {
                    "view": "EFFECT_ALLOW",
                    "comment": "EFFECT_ALLOW",
                    "read": "EFFECT_ALLOW"
                }}],
                "cerbosCallId": "1-2-3-4"
            }
            """.formatted(requestId);
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertTrue(strategy.authorizeRequest(request));
        }
    }

}
