package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class BasicCredentialMetaDataTest {

    private static final File JSON_FILE = new File("basicCredentialMetaData.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeABasicCredentialMetaDataToJson() throws IOException {
        final BasicCredentialMetaData credentialMetaDataWritten = new BasicCredentialMetaData(new UsernamePasswordCredential());

        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);

        final CredentialMetaData credentialMetaDataRead = MAPPER.readValue(JSON_FILE, BasicCredentialMetaData.class);

        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}

