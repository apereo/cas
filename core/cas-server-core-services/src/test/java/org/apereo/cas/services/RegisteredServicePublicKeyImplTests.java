package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServicePublicKeyImplTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "registeredServicePublicKeyImpl.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        final RegisteredServicePublicKeyImpl publicKeyWritten = new RegisteredServicePublicKeyImpl("location", "algorithm");

        MAPPER.writeValue(JSON_FILE, publicKeyWritten);

        final RegisteredServicePublicKey credentialRead = MAPPER.readValue(JSON_FILE, RegisteredServicePublicKeyImpl.class);

        assertEquals(publicKeyWritten, credentialRead);
    }
}
