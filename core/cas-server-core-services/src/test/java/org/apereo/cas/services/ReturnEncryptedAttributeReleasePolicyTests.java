package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

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
@Tag("Simple")
public class ReturnEncryptedAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "EncryptingAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerialization() throws IOException {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("attributeOne");
        val policyWritten = new ReturnEncryptedAttributeReleasePolicy(allowedAttributes);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnEncryptedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
        assertNotNull(policyWritten.toString());
    }

    @Test
    public void verifyNoPublicKey() {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        var results = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(results.isEmpty());

        val servicePublicKey = new RegisteredServicePublicKeyImpl();
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        results = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyBadCipher() {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = mock(RegisteredServicePublicKey.class);
        when(servicePublicKey.getAlgorithm()).thenReturn("BAD");
        when(servicePublicKey.createInstance()).thenReturn(mock(PublicKey.class));
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        val results = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyEncrypt() {
        val policy = new ReturnEncryptedAttributeReleasePolicy(CollectionUtils.wrapList("cn", "uid", "mail"));
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val servicePublicKey = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        when(registeredService.getPublicKey()).thenReturn(servicePublicKey);
        val results = policy.getAttributes(
            CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertEquals(policy.getAllowedAttributes().size(), results.size());
    }
}
