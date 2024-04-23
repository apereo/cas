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
 * This is {@link DefaultRegisteredServiceAcceptableUsagePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("RegisteredService")
class DefaultRegisteredServiceAcceptableUsagePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new DefaultRegisteredServiceAcceptableUsagePolicy();
        policyWritten.setEnabled(true);
        policyWritten.setMessageCode("example.code");
        policyWritten.setText("example text");
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, DefaultRegisteredServiceAcceptableUsagePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
