package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0
 */
public class ReturnMappedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAReturnMappedAttributeReleasePolicyToJson() throws IOException {
        final HashMap<String, String> allowedAttributes = new HashMap<>();
        allowedAttributes.put("keyOne", "valueOne");
        final ReturnMappedAttributeReleasePolicy policyWritten = new ReturnMappedAttributeReleasePolicy(allowedAttributes);

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
