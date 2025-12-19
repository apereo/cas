package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulRegisteredServiceProxyPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RegisteredService")
class RestfulRegisteredServiceProxyPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();

        val policy = new RestfulRegisteredServiceProxyPolicy();
        policy.setEndpoint("http://localhost:9222");
        policy.setHeaders(CollectionUtils.wrap("header", "value"));
        MAPPER.writeValue(jsonFile, policy);
        val readPolicy = MAPPER.readValue(jsonFile, RestfulRegisteredServiceProxyPolicy.class);
        assertEquals(policy, readPolicy);
    }

    @Test
    void verifyOperationPasses() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.OK)) {
            webServer.start();
            val service = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new RestfulRegisteredServiceProxyPolicy();
            policy.setEndpoint("http://localhost:%s".formatted(webServer.getPort()));
            policy.setHeaders(CollectionUtils.wrap("header", "value"));
            assertTrue(policy.isAllowedProxyCallbackUrl(service, new URI("https://proxy.example.org").toURL()));
        }
    }

    @Test
    void verifyOperationFails() throws Throwable {
        try (val webServer = new MockWebServer(HttpStatus.FORBIDDEN)) {
            webServer.start();
            val service = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new RestfulRegisteredServiceProxyPolicy();
            policy.setEndpoint("http://localhost:%s".formatted(webServer.getPort()));
            policy.setHeaders(CollectionUtils.wrap("header", "value"));
            assertFalse(policy.isAllowedProxyCallbackUrl(service, new URI("https://proxy.example.org").toURL()));
        }
    }
}
