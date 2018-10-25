package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServicePublicKeyImplTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServicePublicKeyImpl.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        val publicKeyWritten = new RegisteredServicePublicKeyImpl("location", "algorithm");
        MAPPER.writeValue(JSON_FILE, publicKeyWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, RegisteredServicePublicKeyImpl.class);
        assertEquals(publicKeyWritten, credentialRead);
    }
}
