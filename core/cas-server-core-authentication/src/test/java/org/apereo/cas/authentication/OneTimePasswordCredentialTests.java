package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OneTimePasswordCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oneTimePasswordCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAnOneTimePasswordCredentialToJson() throws IOException {
        final OneTimePasswordCredential credentialWritten = new OneTimePasswordCredential("id", "password");
        MAPPER.writeValue(JSON_FILE, credentialWritten);
        final CredentialMetaData credentialRead = MAPPER.readValue(JSON_FILE, OneTimePasswordCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
