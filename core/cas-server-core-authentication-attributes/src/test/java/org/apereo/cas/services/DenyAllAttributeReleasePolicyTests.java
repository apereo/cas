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
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("AttributeRelease")
class DenyAllAttributeReleasePolicyTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeADenyAllAttributeReleasePolicyToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new DenyAllAttributeReleasePolicy();
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, DenyAllAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
