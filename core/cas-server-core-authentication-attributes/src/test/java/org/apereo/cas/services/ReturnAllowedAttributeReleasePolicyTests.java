package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ReturnAllowedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllowedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAReturnAllowedAttributeReleasePolicyToJson() throws IOException {
        val allowedAttributes = new ArrayList<String>();
        allowedAttributes.add("attributeOne");
        val policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }
}
