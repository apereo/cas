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
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PermifyRegisteredServiceAccessStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("RegisteredService")
@ExtendWith(CasTestExtension.class)
class PermifyRegisteredServiceAccessStrategyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val strategyWritten = new PermifyRegisteredServiceAccessStrategy();
        strategyWritten.setApiUrl("https://localhost:8080");
        strategyWritten.setSubjectRelation("owner");
        strategyWritten.setEntityType("document");
        strategyWritten.setTenantId(UUID.randomUUID().toString());
        strategyWritten.setPermission("delete");
        MAPPER.writeValue(jsonFile, strategyWritten);
        val read = MAPPER.readValue(jsonFile, PermifyRegisteredServiceAccessStrategy.class);
        assertEquals(strategyWritten, read);
    }

    @Test
    void verifyOperation() throws Throwable {
        val strategy = new PermifyRegisteredServiceAccessStrategy();
        strategy.setSubjectRelation("reader");
        strategy.setTenantId("123456");
        strategy.setPermission("view");
        strategy.setToken(UUID.randomUUID().toString());

        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService())
            .principalId("casuser")
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .attributes(CoreAuthenticationTestUtils.getAttributes())
            .build();
        try (val webServer = new MockWebServer(Map.of("can", "CHECK_RESULT_ALLOWED"))) {
            webServer.start();
            strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertTrue(strategy.authorizeRequest(request));
        }
    }

    @Test
    void verifyFailedResponseIsDenied() throws Throwable {
        val strategy = new PermifyRegisteredServiceAccessStrategy();
        strategy.setTenantId("123456");
        strategy.setPermission("view");
        strategy.setToken(UUID.randomUUID().toString());

        val request = RegisteredServiceAccessStrategyRequest.builder()
            .service(RegisteredServiceTestUtils.getService())
            .principalId("casuser")
            .registeredService(RegisteredServiceTestUtils.getRegisteredService())
            .attributes(CoreAuthenticationTestUtils.getAttributes())
            .build();
        for (val status : List.of(HttpStatus.FORBIDDEN, HttpStatus.UNAUTHORIZED, HttpStatus.BAD_REQUEST)) {
            try (val webServer = new MockWebServer("{\"can\":\"CHECK_RESULT_ALLOWED\"}", status)) {
                webServer.start();
                strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
                assertFalse(strategy.authorizeRequest(request));
            }
        }
        try (val webServer = new MockWebServer("not-json", HttpStatus.OK)) {
            webServer.start();
            strategy.setApiUrl("http://localhost:%s".formatted(webServer.getPort()));
            assertFalse(strategy.authorizeRequest(request));
        }
    }
}
