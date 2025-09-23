package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Authentication")
class SimplePrincipalTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "simplePrincipal.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    @Test
    void verifyEquality() {
        val p = new SimplePrincipal("id", new HashMap<>());
        assertNotEquals(null, p);
        assertNotEquals("HelloWorld", p);
    }

    @Test
    void verifySerializeACompletePrincipalToJson() throws Throwable {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("attribute", List.of("value"));
        val principalWritten = new SimplePrincipal("id", attributes);
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

    @Test
    void verifySerializeAPrincipalWithEmptyAttributesToJson() throws Throwable {
        val principalWritten = new SimplePrincipal("id", new HashMap<>());
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

}
