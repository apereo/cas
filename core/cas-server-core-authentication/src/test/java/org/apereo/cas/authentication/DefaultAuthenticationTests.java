package org.apereo.cas.authentication;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for JSON Serialization
 *
 * @author David Rodriguez
 * @since 5.0.0
 */
@Tag("Authentication")
class DefaultAuthenticationTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "defaultAuthentication.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .build().toObjectMapper();

    @Test
    void verifySerializeADefaultAuthenticationToJson() throws Throwable {
        val authn = CoreAuthenticationTestUtils.getAuthentication();
        MAPPER.writeValue(JSON_FILE, authn);
        val authn2 = MAPPER.readValue(JSON_FILE, Authentication.class);
        assertEquals(authn, authn2);
        assertTrue(authn.isEqualTo(authn2));
    }
}
