package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ReturnStaticAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Attributes")
public class ReturnStaticAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "ReturnStaticAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @BeforeEach
    public void setup() {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, CasConfigurationProperties.class,
            CasConfigurationProperties.class.getSimpleName());
        ApplicationContextProvider.holdApplicationContext(applicationContext);
    }

    @Test
    public void verifySerializeToJson() throws IOException {
        val policyWritten = new ReturnStaticAttributeReleasePolicy();
        policyWritten.setAllowedAttributes(CollectionUtils.wrap("Hello", CollectionUtils.wrapList("World")));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnStaticAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyReleaseRules() {
        val policy = new ReturnStaticAttributeReleasePolicy();
        policy.setAllowedAttributes(CollectionUtils.wrap("Hello", CollectionUtils.wrapList("World")));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("cn", List.of("CommonName"), "uid", List.of("casuser")));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAttributeReleasePolicy()).thenReturn(policy);

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("Hello"));
        assertEquals("World", results.get("Hello").get(0));
    }

    @Test
    public void verifyExpressions() {
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
            .principal(principal)
            .build();
        val results = policy.getAttributes(releasePolicyContext);
        assertEquals(1, results.size());
        assertTrue(results.containsKey("Hello"));
        assertEquals("World", results.get("Hello").get(0));
    }
}
