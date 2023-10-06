package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
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

import java.io.File;
import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link ReturnEncryptedAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Attributes")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class ReturnEncryptedAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "EncryptingAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerialization() throws IOException {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("attributeOne");
        val policyWritten = new ReturnEncryptedAttributeReleasePolicy(allowedAttributes);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnEncryptedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
        assertNotNull(policyWritten.toString());
    }

    @Test
    void verifyNoPublicKey() throws Throwable {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .applicationContext(applicationContext)
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        var results = policy.getAttributes(context);
        assertTrue(results.isEmpty());

        val servicePublicKey = new RegisteredServicePublicKeyImpl();
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        results = policy.getAttributes(context);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyBadCipher() throws Throwable {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = mock(RegisteredServicePublicKey.class);
        when(servicePublicKey.getAlgorithm()).thenReturn("BAD");
        when(servicePublicKey.createInstance()).thenReturn(mock(PublicKey.class));
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        val results = policy.getAttributes(context);
        assertTrue(results.isEmpty());
    }

    @Test
    void verifyEncrypt() throws Throwable {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn", "uid", "mail"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        val results = policy.getAttributes(context);
        assertEquals(policy.getAllowedAttributes().size(), results.size());
    }
}
