package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class OneTimePasswordCredentialTest {

    private static final File JSON_FILE = new File("oneTimePasswordCredential.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAnOneTimePasswordCredentialToJson() throws IOException {
        final OneTimePasswordCredential credentialWritten = new OneTimePasswordCredential("id", "password");

        mapper.writeValue(JSON_FILE, credentialWritten);

        final CredentialMetaData credentialRead = mapper.readValue(JSON_FILE, OneTimePasswordCredential.class);

        assertEquals(credentialWritten, credentialRead);
    }
}