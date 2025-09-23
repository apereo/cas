package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnRestfulAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new ReturnRestfulAttributeReleasePolicy()
            .setEndpoint("http://endpoint.example.org")
            .setAllowedAttributes(CollectionUtils.wrap("attribute1", CollectionUtils.wrapList("value1")));
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, ReturnRestfulAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyPolicy() throws Throwable {
        val data = MAPPER.writeValueAsString(CollectionUtils.wrap("givenName", "CASUSER", "familyName", "CAS"));
        try (val webServer = new MockWebServer(new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policyWritten = new ReturnRestfulAttributeReleasePolicy()
                .setEndpoint("http://localhost:%s".formatted(webServer.getPort()));
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
        try (val webServer = new MockWebServer(
            new ByteArrayResource(data.getBytes(StandardCharsets.UTF_8), "REST Output"), MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policyWritten = new ReturnRestfulAttributeReleasePolicy()
                .setEndpoint("http://localhost:%s".formatted(webServer.getPort()))
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
        try (val webServer = new MockWebServer(
            new ByteArrayResource("---".getBytes(StandardCharsets.UTF_8), "REST Output"),
            MediaType.APPLICATION_JSON_VALUE)) {
            webServer.start();
            val policy = new ReturnRestfulAttributeReleasePolicy()
                .setEndpoint("http://localhost:%s".formatted(webServer.getPort()));
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
