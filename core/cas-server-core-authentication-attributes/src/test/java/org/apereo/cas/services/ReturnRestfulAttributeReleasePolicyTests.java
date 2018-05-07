package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link ReturnRestfulAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class ReturnRestfulAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "verifySerializeAReturnAllowedAttributeReleasePolicyToJson.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAttributeReleasePolicyToJson() throws IOException {
        final var policyWritten = new ReturnRestfulAttributeReleasePolicy("http://endpoint.example.org");
        MAPPER.writeValue(JSON_FILE, policyWritten);
        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnRestfulAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyPolicy() throws IOException {
        final var data = MAPPER.writeValueAsString(CollectionUtils.wrap("givenName", "CASUSER", "familyName", "CAS"));
        try (var webServer = new MockWebServer(9299,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();

            final var policyWritten = new ReturnRestfulAttributeReleasePolicy("http://localhost:9299");
            final Map attributes = policyWritten.getAttributes(CoreAuthenticationTestUtils.getPrincipal(),
                CoreAuthenticationTestUtils.getService(),
                CoreAuthenticationTestUtils.getRegisteredService());
            assertFalse(attributes.isEmpty());
        } catch (final Exception e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }
}
