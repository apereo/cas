package org.apereo.cas.digest;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Authentication")
public class DigestCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "digestCredential.json");
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeADigestCredentialToJson() throws IOException {
        val credentialMetaDataWritten = new DigestCredential("uid", "realm", "hash");

        MAPPER.writeValue(JSON_FILE, credentialMetaDataWritten);

        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, DigestCredential.class);

        assertEquals(credentialMetaDataWritten, credentialMetaDataRead);
    }
}
