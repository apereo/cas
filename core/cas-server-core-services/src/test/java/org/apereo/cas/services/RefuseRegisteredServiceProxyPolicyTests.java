package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
public class RefuseRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "refuseRegisteredServiceProxyPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyJson() throws IOException {
        val policyWritten = new RefuseRegisteredServiceProxyPolicy();
        assertFalse(policyWritten.isAllowedProxyCallbackUrl(new URL("https://github.com")));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, RefuseRegisteredServiceProxyPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
