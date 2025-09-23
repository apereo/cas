package org.apereo.cas.authentication.principal;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Tag("Authentication")
class ShibbolethCompatiblePersistentIdGeneratorTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyGenerator() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");
        assertNotNull(generator.toString());
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("testuser");
        val value = generator.generate(principal, CoreAuthenticationTestUtils.getService());
        assertNotNull(value);
    }

    @Test
    void verifyGeneratorByPrincipal() {
        val attrs = (Map) Map.of("uid", List.of("testuser"));
        val generator = new ShibbolethCompatiblePersistentIdGenerator();
        generator.setAttribute("uid");
        assertNotNull(generator.toString());
        assertNotNull(generator.determinePrincipalIdFromAttributes("uid", attrs));

        val principal = mock(Principal.class);
        when(principal.getAttributes()).thenReturn(attrs);
        when(principal.getId()).thenReturn("testuser");
        val value = generator.generate(principal, CoreAuthenticationTestUtils.getService());
        assertNotNull(value);
    }

    @Test
    void realTestOfGeneratorThatVerifiesValueReturned() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("thisisasalt");
        val principal = mock(Principal.class);
        when(principal.getId()).thenReturn("grudkin");
        val value = generator.generate(principal, CoreAuthenticationTestUtils.getService("https://shibboleth.irbmanager.com/"));
        assertEquals("jvZO/wYedArYIEIORGdHoMO4qkw=", value);
    }

    @Test
    void verifyJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val generatorWritten = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");
        MAPPER.writeValue(jsonFile, generatorWritten);
        val credentialRead = MAPPER.readValue(jsonFile, ShibbolethCompatiblePersistentIdGenerator.class);
        assertEquals(generatorWritten, credentialRead);
    }
}
