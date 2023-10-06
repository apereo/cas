package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("RegisteredService")
class RefuseRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "refuseRegisteredServiceProxyPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyJson() throws Throwable {
        val policyWritten = new RefuseRegisteredServiceProxyPolicy();
        assertFalse(policyWritten.isAllowedProxyCallbackUrl(RegisteredServiceTestUtils.getRegisteredService(),
            new URI("https://github.com").toURL()));
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, RefuseRegisteredServiceProxyPolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
