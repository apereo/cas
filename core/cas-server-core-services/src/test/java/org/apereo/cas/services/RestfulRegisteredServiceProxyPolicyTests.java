package org.apereo.cas.services;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RestfulRegisteredServiceProxyPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("RegisteredService")
class RestfulRegisteredServiceProxyPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "RestfulRegisteredServiceProxyPolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerialization() throws Throwable {
        val policy = new RestfulRegisteredServiceProxyPolicy();
        policy.setEndpoint("http://localhost:9222");
        policy.setHeaders(CollectionUtils.wrap("header", "value"));
        MAPPER.writeValue(JSON_FILE, policy);
        val readPolicy = MAPPER.readValue(JSON_FILE, RestfulRegisteredServiceProxyPolicy.class);
        assertEquals(policy, readPolicy);
    }

    @Test
    void verifyOperationPasses() throws Throwable {
        try (val webServer = new MockWebServer(9222,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val service = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new RestfulRegisteredServiceProxyPolicy();
            policy.setEndpoint("http://localhost:9222");
            policy.setHeaders(CollectionUtils.wrap("header", "value"));
            assertTrue(policy.isAllowedProxyCallbackUrl(service, new URI("https://proxy.example.org").toURL()));
        }
    }

    @Test
    void verifyOperationFails() throws Throwable {
        try (val webServer = new MockWebServer(9222,
            new ByteArrayResource(StringUtils.EMPTY.getBytes(StandardCharsets.UTF_8), "Output"), HttpStatus.FORBIDDEN)) {
            webServer.start();
            val service = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new RestfulRegisteredServiceProxyPolicy();
            policy.setEndpoint("http://localhost:9222");
            policy.setHeaders(CollectionUtils.wrap("header", "value"));
            assertFalse(policy.isAllowedProxyCallbackUrl(service, new URI("https://proxy.example.org").toURL()));
        }
    }
}
