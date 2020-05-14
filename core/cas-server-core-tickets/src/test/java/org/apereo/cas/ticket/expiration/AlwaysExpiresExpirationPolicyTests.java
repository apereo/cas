package org.apereo.cas.ticket.expiration;

import org.apereo.cas.util.serialization.SerializationUtils;

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
 * @since 3.0
 */
@Tag("Simple")
public class AlwaysExpiresExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "alwaysExpiresExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAnAlwaysExpiresExpirationPolicyToJson() throws IOException {
        val policyWritten = new AlwaysExpiresExpirationPolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, AlwaysExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifySerialization() {
        val policyWritten = new AlwaysExpiresExpirationPolicy();
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, AlwaysExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
