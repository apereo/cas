package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
class RegisteredServicePublicKeyImplTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeAX509CertificateCredentialToJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val publicKeyWritten = new RegisteredServicePublicKeyImpl("location", "algorithm");
        MAPPER.writeValue(jsonFile, publicKeyWritten);
        val credentialRead = MAPPER.readValue(jsonFile, RegisteredServicePublicKeyImpl.class);
        assertEquals(publicKeyWritten, credentialRead);
    }

    @Test
    void verifyInstance() throws Throwable {
        val key1 = new RegisteredServicePublicKeyImpl("classpath:keys/RSA1024Public.key", "RSA");
        assertNotNull(key1.createInstance());
        val key2 = new RegisteredServicePublicKeyImpl(null, "RSA");
        assertNull(key2.createInstance());

        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        MAPPER.writeValue(jsonFile, key1);
        val keyRead = MAPPER.readValue(jsonFile, RegisteredServicePublicKeyImpl.class);
        assertEquals(key1, keyRead);
    }
}
