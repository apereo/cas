package org.apereo.cas.authentication;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    @Test
    void verifyUpdateAttributes() {
        val principal1 = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("cn", List.of("Apereo")));
        val authentication1 = CoreAuthenticationTestUtils.getAuthentication(principal1, Map.of("method", List.of("simple")));
        assertEquals("simple", authentication1.getSingleValuedAttribute("method"));
        val principal2 = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("cn", List.of("CAS"), "name", List.of("casuser", "casperson")));
        val authentication2 = CoreAuthenticationTestUtils.getAuthentication(principal2, Map.of("method", List.of("strong")));
        authentication1.updateAttributes(authentication2);
        assertTrue(authentication1.getAttributes().get("method").containsAll(List.of("simple", "strong")));
        assertTrue(authentication1.getPrincipal().getAttributes().get("cn").containsAll(List.of("Apereo", "CAS")));
        assertTrue(authentication1.getPrincipal().getAttributes().get("name").containsAll(List.of("casuser", "casperson")));
    }

    @Test
    void verifyReplaceAttributes() {
        val principal1 = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("cn", List.of("Apereo")));
        val authentication1 = CoreAuthenticationTestUtils.getAuthentication(principal1, Map.of("method", List.of("simple")));
        val principal2 = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(), Map.of("cn", List.of("CAS"), "name", List.of("casuser", "casperson")));
        val authentication2 = CoreAuthenticationTestUtils.getAuthentication(principal2, Map.of("method", List.of("strong")));
        authentication1.replaceAttributes(authentication2);
        assertTrue(authentication1.getAttributes().get("method").contains("strong"));
        assertTrue(authentication1.getPrincipal().getAttributes().get("cn").contains("CAS"));
        assertTrue(authentication1.getPrincipal().getAttributes().get("name").containsAll(List.of("casuser", "casperson")));
    }
}
