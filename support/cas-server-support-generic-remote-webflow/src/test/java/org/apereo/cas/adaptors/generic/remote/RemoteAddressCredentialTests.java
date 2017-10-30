package org.apereo.cas.adaptors.generic.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RemoteAddressCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "remoteAddressCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeARemoteAddressCredentialToJson() throws IOException {
        final RemoteAddressCredential credentialWritten = new RemoteAddressCredential("80.123.456.78");
        MAPPER.writeValue(JSON_FILE, credentialWritten);
        final CredentialMetaData credentialRead = MAPPER.readValue(JSON_FILE, RemoteAddressCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
