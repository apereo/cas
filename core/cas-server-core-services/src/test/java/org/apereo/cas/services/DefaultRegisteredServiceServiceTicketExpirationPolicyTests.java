package org.apereo.cas.services;

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
 * This is {@link DefaultRegisteredServiceServiceTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceServiceTicketExpirationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializationToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val p = new DefaultRegisteredServiceServiceTicketExpirationPolicy();
        p.setNumberOfUses(12);
        p.setTimeToLive("60");
        MAPPER.writeValue(jsonFile, p);
        val repositoryRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceServiceTicketExpirationPolicy.class);
        assertEquals(p, repositoryRead);
    }

    @Test
    void verifyUndefined() {
        val p = RegisteredServiceServiceTicketExpirationPolicy.undefined();
        assertNull(p.getTimeToLive());
        assertEquals(Long.MIN_VALUE, p.getNumberOfUses());
    }
}
