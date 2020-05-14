package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.OneTimePasswordCredential;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Simple")
public class OneTimePasswordCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "oneTimePasswordCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    @SneakyThrows
    public void verifySerializeAnOneTimePasswordCredentialToJson() {
        val credentialWritten = new OneTimePasswordCredential("id", "password");
        MAPPER.writeValue(JSON_FILE, credentialWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, OneTimePasswordCredential.class);
        assertEquals(credentialWritten, credentialRead);
    }
}
