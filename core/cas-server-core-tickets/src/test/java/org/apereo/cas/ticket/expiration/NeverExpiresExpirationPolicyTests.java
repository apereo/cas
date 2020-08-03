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
 * @since 4.1
 */
@Tag("Simple")
public class NeverExpiresExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "neverExpiresExpirationPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeANeverExpiresExpirationPolicyToJson() throws IOException {
        val policyWritten = NeverExpiresExpirationPolicy.INSTANCE;
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, NeverExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifySerialization() {
        val policyWritten = new NeverExpiresExpirationPolicy();
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, NeverExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
