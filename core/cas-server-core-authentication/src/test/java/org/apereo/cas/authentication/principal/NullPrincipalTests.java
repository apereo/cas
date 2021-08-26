package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Tag("Authentication")
public class NullPrincipalTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "nullPrincipal.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeANullPrincipalToJson() throws Exception {
        val serviceWritten = NullPrincipal.getInstance();
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, NullPrincipal.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
