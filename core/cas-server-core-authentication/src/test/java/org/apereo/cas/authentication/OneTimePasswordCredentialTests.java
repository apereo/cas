package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.OneTimePasswordCredential;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OneTimePasswordCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oneTimePasswordCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAnOneTimePasswordCredentialToJson() throws IOException {
        val credentialWritten = new OneTimePasswordCredential("id", "password");
        MAPPER.writeValue(JSON_FILE, credentialWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, OneTimePasswordCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
