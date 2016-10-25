package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GroovyScriptAttributeReleasePolicyTest {

    private static final File JSON_FILE = new File("groovyScriptAttributeReleasePolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAGroovyScriptAttributeReleasePolicyToJson() throws IOException {
        final GroovyScriptAttributeReleasePolicy policyWritten = new GroovyScriptAttributeReleasePolicy();

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = mapper.readValue(JSON_FILE, GroovyScriptAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}