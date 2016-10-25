package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RegisteredServicePublicKeyImplTest {

    private static final File JSON_FILE = new File("registeredServicePublicKeyImpl.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAX509CertificateCredentialToJson() throws IOException {
        RegisteredServicePublicKeyImpl publicKeyWritten = new RegisteredServicePublicKeyImpl("location", "algorithm");

        mapper.writeValue(JSON_FILE, publicKeyWritten);

        final RegisteredServicePublicKey credentialRead = mapper.readValue(JSON_FILE, RegisteredServicePublicKeyImpl.class);

        assertEquals(publicKeyWritten, credentialRead);
    }
}