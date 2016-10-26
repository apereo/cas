package org.apereo.cas.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultAuthenticationTests {

    private static final File JSON_FILE = new File("defaultAuthentication.json");

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeADefaultAuthenticationToJson() throws IOException {
        final Authentication serviceWritten = TestUtils.getAuthentication();

        mapper.writeValue(JSON_FILE, serviceWritten);

        final Authentication serviceRead = mapper.readValue(JSON_FILE, Authentication.class);

        assertEquals(serviceWritten, serviceRead);
    }
}
