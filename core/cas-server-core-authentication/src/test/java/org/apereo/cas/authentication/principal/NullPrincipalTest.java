package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Misagh Moayyed
 * @since 5.0
 */
public class NullPrincipalTest {

    private static final File JSON_FILE = new File("nullPrincipal.json");

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeANullPrincipalToJson() throws IOException {
        final NullPrincipal serviceWritten = NullPrincipal.getInstance();

        mapper.writeValue(JSON_FILE, serviceWritten);

        final NullPrincipal serviceRead = mapper.readValue(JSON_FILE, NullPrincipal.class);

        assertEquals(serviceWritten, serviceRead);
    }
}
