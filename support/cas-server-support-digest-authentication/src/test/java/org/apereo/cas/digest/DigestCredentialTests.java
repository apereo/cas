package org.apereo.cas.digest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.authentication.CredentialMetaData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DigestCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "digestCredential.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeADigestCredentialToJson() throws IOException {
        final DigestCredential credentialMetaDataWritten = new DigestCredential("uid", "realm", "hash");

        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);

        final CredentialMetaData credentialMetaDataRead = MAPPER.readValue(JSON_FILE, DigestCredential.class);

        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
