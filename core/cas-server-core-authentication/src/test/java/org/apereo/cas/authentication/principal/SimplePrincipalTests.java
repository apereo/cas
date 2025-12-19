package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Authentication")
class SimplePrincipalTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "SimplePrincipal.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyEquality() {
        val p = new SimplePrincipal("id", new HashMap<>());
        assertNotEquals(null, p);
        assertNotEquals("HelloWorld", p);
    }

    @Test
    void verifySerializeACompletePrincipalToJson() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("attribute", List.of("value"));
        val principalWritten = new SimplePrincipal("id", attributes);
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

    @Test
    void verifySerializeAPrincipalWithEmptyAttributesToJson() {
        val principalWritten = new SimplePrincipal("id", new HashMap<>());
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

}
