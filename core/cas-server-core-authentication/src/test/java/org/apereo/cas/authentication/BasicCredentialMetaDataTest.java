package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BasicCredentialMetaDataTest {

    private static final File JSON_FILE = new File("basicCredentialMetaData.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeABasicCredentialMetaDataToJson() throws IOException {
        BasicCredentialMetaData credentialMetaDataWritten = new BasicCredentialMetaData(new UsernamePasswordCredential());

        mapper.writeValue(JSON_FILE, credentialMetaDataWritten);

        final CredentialMetaData credentialMetaDataRead = mapper.readValue(JSON_FILE, BasicCredentialMetaData.class);

        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}