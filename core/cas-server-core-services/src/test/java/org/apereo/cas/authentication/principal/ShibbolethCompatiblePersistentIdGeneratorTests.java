package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGeneratorTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "shibbolethCompatiblePersistentIdGenerator.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifyGenerator() {
        final ShibbolethCompatiblePersistentIdGenerator generator = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("testuser");
        final String value = generator.generate(p, RegisteredServiceTestUtils.getService());

        assertNotNull(value);
    }

    @Test
    public void realTestofGeneratorThatVerifiesValueReturned() {
        final ShibbolethCompatiblePersistentIdGenerator generator = new ShibbolethCompatiblePersistentIdGenerator("thisisasalt");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("grudkin");
        final Service s = mock(Service.class);
        when(s.getId()).thenReturn("https://shibboleth.irbmanager.com/");

        final String value = generator.generate(p, s);
        assertEquals("jvZO/wYedArYIEIORGdHoMO4qkw=", value);
    }

    @Test
    public void verifySerializeAShibbolethCompatiblePersistentIdGeneratorToJson() throws IOException {
        final ShibbolethCompatiblePersistentIdGenerator generatorWritten = new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        MAPPER.writeValue(JSON_FILE, generatorWritten);

        final PersistentIdGenerator credentialRead = MAPPER.readValue(JSON_FILE, ShibbolethCompatiblePersistentIdGenerator.class);

        assertEquals(generatorWritten, credentialRead);
    }
}
