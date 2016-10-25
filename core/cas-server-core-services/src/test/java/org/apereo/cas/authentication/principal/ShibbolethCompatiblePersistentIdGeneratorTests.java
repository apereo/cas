package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.services.TestUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class ShibbolethCompatiblePersistentIdGeneratorTests {

    private static final File JSON_FILE = new File("shibbolethCompatiblePersistentIdGenerator.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifyGenerator() {
        final ShibbolethCompatiblePersistentIdGenerator generator =
                new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        final Principal p = mock(Principal.class);
        when(p.getId()).thenReturn("testuser");
        final String value = generator.generate(p, TestUtils.getService());

        assertNotNull(value);
    }

    @Test
    public void verifySerializeAShibbolethCompatiblePersistentIdGeneratorToJson() throws IOException {
        final ShibbolethCompatiblePersistentIdGenerator generatorWritten =
                new ShibbolethCompatiblePersistentIdGenerator("scottssalt");

        mapper.writeValue(JSON_FILE, generatorWritten);

        final PersistentIdGenerator credentialRead = mapper.readValue(JSON_FILE, ShibbolethCompatiblePersistentIdGenerator.class);

        assertEquals(generatorWritten, credentialRead);
    }
}
