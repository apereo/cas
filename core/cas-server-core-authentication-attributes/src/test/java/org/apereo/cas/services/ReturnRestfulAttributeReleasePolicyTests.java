package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ReturnRestfulAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("RestfulApi")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnRestfulAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "ReturnRestfulAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyJson() throws IOException {
        val policyWritten = new ReturnRestfulAttributeReleasePolicy()
            .setEndpoint("http://endpoint.example.org")
            .setAllowedAttributes(CollectionUtils.wrap("attribute1", CollectionUtils.wrapList("value1")));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnRestfulAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyPolicy() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("givenName", "CASUSER", "familyName", "CAS"));
        try (val webServer = new MockWebServer(9299,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policyWritten = new ReturnRestfulAttributeReleasePolicy().setEndpoint("http://localhost:9299");
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .applicationContext(applicationContext)
                .build();
            val attributes = policyWritten.getAttributes(releasePolicyContext);
            assertFalse(attributes.isEmpty());
        }
    }

    @Test
    void verifyPolicyWithMappedAttributes() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("givenName", "CASUSER"));
        try (val webServer = new MockWebServer(9299,
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policyWritten = new ReturnRestfulAttributeReleasePolicy()
                .setEndpoint("http://localhost:9299")
                .setAllowedAttributes(Map.of("givenName", List.of("givenName1", "givenName2")));

            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .applicationContext(applicationContext)
                .build();
            val attributes = policyWritten.getAttributes(releasePolicyContext);
            assertEquals(2, attributes.size());
            assertTrue(attributes.containsKey("givenName1"));
            assertTrue(attributes.containsKey("givenName2"));
        }
    }

    @Test
    void verifyBadPolicy() throws Throwable {
        try (val webServer = new MockWebServer(9298,
            new ByteArrayResource("---".getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policy = new ReturnRestfulAttributeReleasePolicy().setEndpoint("http://localhost:9298");
            val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
                .service(CoreAuthenticationTestUtils.getService())
                .principal(CoreAuthenticationTestUtils.getPrincipal())
                .applicationContext(applicationContext)
                .build();
            val attributes = policy.getAttributes(releasePolicyContext);
            assertTrue(attributes.isEmpty());
        }
    }
}
