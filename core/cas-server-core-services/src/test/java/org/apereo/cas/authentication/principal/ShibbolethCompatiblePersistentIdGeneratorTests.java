package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredServiceTestUtils;
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
@Tag("RegisteredService")
class ShibbolethCompatiblePersistentIdGeneratorTests {

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyGenerator() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");
        assertNotNull(generator.toString());
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("testuser");
        val value = generator.generate(p, RegisteredServiceTestUtils.getService());
        assertNotNull(value);
    }

    @Test
    void verifyGeneratorByPrincipal() {
        val attrs = (Map) Map.of("uid", List.of("testuser"));
        val generator = new ShibbolethCompatiblePersistentIdGenerator();
        generator.setAttribute("uid");
        assertNotNull(generator.toString());
        assertNotNull(generator.determinePrincipalIdFromAttributes("uid", attrs));

        val p = mock(Principal.class);
        when(p.getAttributes()).thenReturn(attrs);
        when(p.getId()).thenReturn("testuser");
        val value = generator.generate(p, RegisteredServiceTestUtils.getService());
        assertNotNull(value);
    }

    @Test
    void realTestOfGeneratorThatVerifiesValueReturned() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("thisisasalt");

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("grudkin");
        val s = mock(Service.class);
        when(s.getId()).thenReturn("https://shibboleth.irbmanager.com/");

        val value = generator.generate(p, s);
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
