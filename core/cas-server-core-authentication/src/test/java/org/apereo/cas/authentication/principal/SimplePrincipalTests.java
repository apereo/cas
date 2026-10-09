package org.apereo.cas.authentication.principal;

import module java.base;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
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

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyEquality() {
        val principal = new SimplePrincipal("id", new HashMap<>());
        assertNotEquals(null, principal);
        assertNotEquals("HelloWorld", principal);
    }

    @Test
    void verifySerializeACompletePrincipalToJson() {
        val attributes = new HashMap<String, List<Object>>();
        attributes.put("attribute", List.of("value"));
        val principalWritten = new SimplePrincipal("id", attributes);
        val json = MAPPER.writeValueAsString(principalWritten);
        val principalRead = MAPPER.readValue(json, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

    @Test
    void verifySerializeAPrincipalWithEmptyAttributesToJson() {
        val principalWritten = new SimplePrincipal("id", new HashMap<>());
        val json = MAPPER.writeValueAsString(principalWritten);
        val principalRead = MAPPER.readValue(json, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

    @Test
    void verifyAttributes() {
        val principal = new SimplePrincipal("id", Map.of("givenName", List.of("CAS")));
        assertTrue(principal.getAttributes().containsKey("givenName"));
        assertTrue(principal.getAttributes().containsKey("givenname"));
        
    }

}
