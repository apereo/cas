package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
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
@Tag("Simple")
public class NullPrincipalTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "nullPrincipal.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    @SneakyThrows
    public void verifySerializeANullPrincipalToJson() {
        val serviceWritten = NullPrincipal.getInstance();
        MAPPER.writeValue(JSON_FILE, serviceWritten);
        val serviceRead = MAPPER.readValue(JSON_FILE, NullPrincipal.class);
        assertEquals(serviceWritten, serviceRead);
    }
}
