package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class SimplePrincipalTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "simplePrincipal.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeACompletePrincipalToJson() throws IOException {
        val attributes = new HashMap<String, Object>();
        attributes.put("attribute", "value");
        val principalWritten = new SimplePrincipal("id", attributes);
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

    @Test
    public void verifySerializeAPrincipalWithEmptyAttributesToJson() throws IOException {
        val principalWritten = new SimplePrincipal("id", new HashMap<>(0));
        MAPPER.writeValue(JSON_FILE, principalWritten);
        val principalRead = MAPPER.readValue(JSON_FILE, SimplePrincipal.class);
        assertEquals(principalWritten, principalRead);
    }

}
