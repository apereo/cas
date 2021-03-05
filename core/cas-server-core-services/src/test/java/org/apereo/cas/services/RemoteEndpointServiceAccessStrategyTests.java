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

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("RegisteredService")
public class RemoteEndpointServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "remoteEndpointServiceAccessStrategy.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        val strategyWritten = new RemoteEndpointServiceAccessStrategy();
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, RemoteEndpointServiceAccessStrategy.class);
        assertEquals(strategyWritten, credentialRead);
    }

    @Test
    public void verifyOperation() {
        val strategy = new RemoteEndpointServiceAccessStrategy();
        strategy.setEndpointUrl("http://localhost:8755");
        strategy.setAcceptableResponseCodes("200,201");
        try (val webServer = new MockWebServer(8755,
            new ByteArrayResource("OK".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.doPrincipalAttributesAllowServiceAccess("casuser", CoreAuthenticationTestUtils.getAttributes()));
        }
    }

    @Test
    public void verifyFails() {
        val strategy = new RemoteEndpointServiceAccessStrategy();
        strategy.setEndpointUrl("http://localhost:1234");
        strategy.setAcceptableResponseCodes("600");
        try (val webServer = new MockWebServer(8756,
            new ByteArrayResource("OK".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertFalse(strategy.doPrincipalAttributesAllowServiceAccess("casuser", CoreAuthenticationTestUtils.getAttributes()));
        }
    }
}
