package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
@Tag("RegisteredService")
public class ShibbolethCompatiblePersistentIdGeneratorTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "shibbolethCompatiblePersistentIdGenerator.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyGenerator() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");
        assertNotNull(generator.toString());
        val p = mock(Principal.class);
        when(p.getId()).thenReturn("testuser");
        val value = generator.generate(p, RegisteredServiceTestUtils.getService());
        assertNotNull(value);
    }

    @Test
    public void verifyGeneratorByPrincipal() {
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
    public void realTestOfGeneratorThatVerifiesValueReturned() {
        val generator = new ShibbolethCompatiblePersistentIdGenerator("thisisasalt");

        val p = mock(Principal.class);
        when(p.getId()).thenReturn("grudkin");
        val s = mock(Service.class);
        when(s.getId()).thenReturn("https://shibboleth.irbmanager.com/");

        val value = generator.generate(p, s);
        assertEquals("jvZO/wYedArYIEIORGdHoMO4qkw=", value);
    }

    @Test
    public void verifyJson() throws IOException {
        val generatorWritten = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");
        MAPPER.writeValue(JSON_FILE, generatorWritten);
        val credentialRead = MAPPER.readValue(JSON_FILE, ShibbolethCompatiblePersistentIdGenerator.class);
        assertEquals(generatorWritten, credentialRead);
    }
}
