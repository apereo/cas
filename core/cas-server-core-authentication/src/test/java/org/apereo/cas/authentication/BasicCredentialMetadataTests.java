package org.apereo.cas.authentication;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetadata;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
@Tag("AuthenticationMetadata")
class BasicCredentialMetadataTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "basicCredentialMetaData.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeABasicCredentialMetaDataToJson() throws Throwable {
        val cmd = new BasicCredentialMetadata(new UsernamePasswordCredential());
        cmd.putProperty("key", "value").putProperties(Map.of("one", "two"));
        assertTrue(cmd.containsProperty("one"));
        assertNotNull(cmd.getProperty("key", String.class));
        MAPPER.writeValue(JSON_FILE, cmd);
        val credentialMetaDataRead = MAPPER.readValue(JSON_FILE, BasicCredentialMetadata.class);
        assertEquals(cmd, credentialMetaDataRead);
    }
}

