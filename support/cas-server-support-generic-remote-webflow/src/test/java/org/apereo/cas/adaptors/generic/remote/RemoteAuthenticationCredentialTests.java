package org.apereo.cas.adaptors.generic.remote;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Authentication")
class RemoteAuthenticationCredentialTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeARemoteAddressCredentialToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val credentialWritten = new RemoteAuthenticationCredential("80.123.456.78");
        credentialWritten.setCookie("HelloWorld");
        MAPPER.writeValue(jsonFile, credentialWritten);
        val credentialRead = MAPPER.readValue(jsonFile, RemoteAuthenticationCredential.class);
        assertEquals(credentialWritten, credentialRead);
        assertEquals("HelloWorld", credentialRead.getId());
    }
}
