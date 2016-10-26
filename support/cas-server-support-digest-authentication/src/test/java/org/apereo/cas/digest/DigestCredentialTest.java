package org.apereo.cas.digest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.CredentialMetaData;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DigestCredentialTest {

    private static final File JSON_FILE = new File("digestCredential.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeADigestCredentialToJson() throws IOException {
        final DigestCredential credentialMetaDataWritten = new DigestCredential("uid", "realm", "hash");

        mapper.writeValue(JSON_FILE, credentialMetaDataWritten);

        final CredentialMetaData credentialMetaDataRead = mapper.readValue(JSON_FILE, DigestCredential.class);

        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
