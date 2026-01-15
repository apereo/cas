package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceProxyTicketExpirationPolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceProxyTicketExpirationPolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializationToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val p = new DefaultRegisteredServiceProxyTicketExpirationPolicy();
        p.setNumberOfUses(12);
        p.setTimeToLive("60");
        MAPPER.writeValue(jsonFile, p);
        val repositoryRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceProxyTicketExpirationPolicy.class);
        assertEquals(p, repositoryRead);
    }
}
