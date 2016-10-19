package org.apereo.cas.authentication.principal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class SimplePrincipalTest {

    private static final File JSON_FILE = new File("simplePrincipal.json");

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeACompletePrincipalToJson() throws IOException {
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put("attribute", "value");
        SimplePrincipal principalWritten = new SimplePrincipal("id", attributes);

        mapper.writeValue(JSON_FILE, principalWritten);

        final SimplePrincipal principalRead = mapper.readValue(JSON_FILE, SimplePrincipal.class);

        assertEquals(principalWritten, principalRead);
    }

    @Test
    public void verifySerializeAPrincipalWithEmptyAttributesToJson() throws IOException {
        SimplePrincipal principalWritten = new SimplePrincipal("id", Collections.emptyMap());

        mapper.writeValue(JSON_FILE, principalWritten);

        final SimplePrincipal principalRead = mapper.readValue(JSON_FILE, SimplePrincipal.class);

        assertEquals(principalWritten, principalRead);
    }

}