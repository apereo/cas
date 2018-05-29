package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RemoteEndpointServiceAccessStrategyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "remoteEndpointServiceAccessStrategy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        final var strategyWritten = new RemoteEndpointServiceAccessStrategy();
        MAPPER.writeValue(JSON_FILE, strategyWritten);
        final RegisteredServiceAccessStrategy credentialRead = MAPPER.readValue(JSON_FILE, RemoteEndpointServiceAccessStrategy.class);
        assertEquals(strategyWritten, credentialRead);
    }

    @Test
    public void verifyOperation() {
        final var strategy = new RemoteEndpointServiceAccessStrategy();
        strategy.setEndpointUrl("http://localhost:8755");
        strategy.setAcceptableResponseCodes("200,201");
        try (var webServer = new MockWebServer(8755,
            new ByteArrayResource("OK".getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            assertTrue(strategy.doPrincipalAttributesAllowServiceAccess("casuser", CoreAuthenticationTestUtils.getAttributes()));
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
