package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CerbosRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("RegisteredService")
class CerbosRegisteredServiceAccessStrategyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "CerbosRegisteredServiceAccessStrategy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeToJson() throws IOException {
        val strategyWritten = new CerbosRegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setActions(List.of("read", "write", "view"));
        strategyWritten.setAuxData(Map.of("key", "value"));
        strategyWritten.setToken(UUID.randomUUID().toString());
        strategyWritten.setRolesAttribute("memberOf");
        strategyWritten.setScope("scope1");
        strategyWritten.setKind("kind1");
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val read = MAPPER.readValue(JSON_FILE, CerbosRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    void verifyOperation() throws Throwable {
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
        try (val webServer = new MockWebServer(3592,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.authorizeRequest(request));
        }
    }

}
