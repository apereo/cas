package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DenyAllAttributeReleasePolicyTest {

    private static final File JSON_FILE = new File("denyAllAttributeReleasePolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeADenyAllAttributeReleasePolicyToJson() throws IOException {
        final DenyAllAttributeReleasePolicy policyWritten = new DenyAllAttributeReleasePolicy();

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = mapper.readValue(JSON_FILE, DenyAllAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}