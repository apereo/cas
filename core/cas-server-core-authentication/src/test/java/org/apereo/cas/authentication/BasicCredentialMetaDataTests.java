package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class BasicCredentialMetaDataTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "basicCredentialMetaData.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    @SneakyThrows
    public void verifySerializeABasicCredentialMetaDataToJson() {
        val credentialMetaDataWritten = new BasicCredentialMetaData(new UsernamePasswordCredential());
        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, BasicCredentialMetaData.class);
        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }

    @Test
    public void verifyBadCredential() {
        assertThrows(IllegalArgumentException.class, () -> new BasicCredentialMetaData(mock(Credential.class)));
    }
}

