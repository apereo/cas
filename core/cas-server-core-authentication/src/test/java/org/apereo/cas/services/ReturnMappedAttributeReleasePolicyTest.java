package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class ReturnMappedAttributeReleasePolicyTest {

    private static final File JSON_FILE = new File("returnMappedAttributeReleasePolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAReturnMappedAttributeReleasePolicyToJson() throws IOException {
        HashMap<String, String> allowedAttributes = new HashMap<>();
        allowedAttributes.put("keyOne", "valueOne");
        final ReturnMappedAttributeReleasePolicy policyWritten = new ReturnMappedAttributeReleasePolicy(allowedAttributes);

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = mapper.readValue(JSON_FILE, ReturnMappedAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}