package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.CollectionUtils;
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ReturnStaticAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("AttributeRelease")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@ExtendWith(CasTestExtension.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnStaticAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new ReturnStaticAttributeReleasePolicy();
        policyWritten.setAllowedAttributes(CollectionUtils.wrap("Hello", CollectionUtils.wrapList("World")));
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, ReturnStaticAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyReleaseRules() throws Throwable {
        val policy = new ReturnStaticAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap("Hello", CollectionUtils.wrapList("World")));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("Hello"));
        assertEquals("World", results.get("Hello").getFirst());
    }

    @Test
    void verifyExpressions() throws Throwable {
        System.setProperty("MY_ATTR", "World");
        val policy = new ReturnStaticAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap("Hello",
            CollectionUtils.wrapList("${#systemProperties['MY_ATTR']}")));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("Hello"));
        assertEquals("World", results.get("Hello").getFirst());
    }
}
