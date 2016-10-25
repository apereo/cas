package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReturnAllowedAttributeReleasePolicyTest {

    private static final File JSON_FILE = new File("returnAllowedAttributeReleasePolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeAReturnAllowedAttributeReleasePolicyToJson() throws IOException {
        List<String> allowedAttributes = new ArrayList<>();
        allowedAttributes.add("attributeOne");
        final ReturnAllowedAttributeReleasePolicy policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = mapper.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}