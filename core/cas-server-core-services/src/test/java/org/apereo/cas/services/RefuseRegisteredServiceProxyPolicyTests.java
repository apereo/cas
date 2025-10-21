package org.apereo.cas.services;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
class RefuseRegisteredServiceProxyPolicyTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyJson() throws Throwable {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new RefuseRegisteredServiceProxyPolicy();
        assertFalse(policyWritten.isAllowedProxyCallbackUrl(RegisteredServiceTestUtils.getRegisteredService(),
            new URI("https://github.com").toURL()));
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, RefuseRegisteredServiceProxyPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
