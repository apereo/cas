package org.apereo.cas.adaptors.generic.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.CredentialMetaData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RemoteAddressCredentialTest {

    private static final File JSON_FILE = new File("remoteAddressCredential.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeARemoteAddressCredentialToJson() throws IOException {
        final RemoteAddressCredential credentialWritten = new RemoteAddressCredential("80.123.456.78");

        mapper.writeValue(JSON_FILE, credentialWritten);

        final CredentialMetaData credentialRead = mapper.readValue(JSON_FILE, RemoteAddressCredential.class);

        assertEquals(credentialWritten, credentialRead);
    }
}