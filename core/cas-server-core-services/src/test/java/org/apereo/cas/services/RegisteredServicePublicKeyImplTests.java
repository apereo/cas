package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
public class RegisteredServicePublicKeyImplTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServicePublicKeyImpl.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws Exception {
        val publicKeyWritten = new RegisteredServicePublicKeyImpl("location", "algorithm");
        MAPPER.writeValue(JSON_FILE, publicKeyWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, RegisteredServicePublicKeyImpl.class);
        assertEquals(publicKeyWritten, credentialRead);
    }

    @Test
    public void verifyInstance() {
        val key1 = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        assertNotNull(key1.createInstance());

        val key2 = new RegisteredServicePublicKeyImpl(null, "RSA");
        assertNull(key2.createInstance());
    }
}
