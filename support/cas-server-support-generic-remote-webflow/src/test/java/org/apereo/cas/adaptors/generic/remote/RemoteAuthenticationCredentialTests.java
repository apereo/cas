package org.apereo.cas.adaptors.generic.remote;

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
 * @since 5.0.0
 */
@Tag("Authentication")
class RemoteAuthenticationCredentialTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "remoteAddressCredential.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeARemoteAddressCredentialToJson() throws IOException {
        val credentialWritten = new RemoteAuthenticationCredential("80.123.456.78");
        credentialWritten.setCookie("HelloWorld");
        MAPPER.writeValue(JSON_FILE, credentialWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, RemoteAuthenticationCredential.class);
        assertEquals(credentialWritten, credentialRead);
        assertEquals("HelloWorld", credentialRead.getId());
    }
}
