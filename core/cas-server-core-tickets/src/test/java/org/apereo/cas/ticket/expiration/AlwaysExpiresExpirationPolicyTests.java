package org.apereo.cas.ticket.expiration;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.serialization.SerializationUtils;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link AlwaysExpiresExpirationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 3.0
 */
@Tag("ExpirationPolicy")
class AlwaysExpiresExpirationPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "alwaysExpiresExpirationPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeAnAlwaysExpiresExpirationPolicyToJson() {
        val policyWritten = AlwaysExpiresExpirationPolicy.INSTANCE;
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, AlwaysExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifySerialization() {
        val policyWritten = AlwaysExpiresExpirationPolicy.INSTANCE;
        val result = SerializationUtils.serialize(policyWritten);
        val policyRead = SerializationUtils.deserialize(result, AlwaysExpiresExpirationPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
