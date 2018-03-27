package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RunWith(JUnit4.class)
@Slf4j
public class ReturnAllowedAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "returnAllowedAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeAReturnAllowedAttributeReleasePolicyToJson() throws IOException {
        final List<String> allowedAttributes = new ArrayList<>();
        allowedAttributes.add("attributeOne");
        final ReturnAllowedAttributeReleasePolicy policyWritten = new ReturnAllowedAttributeReleasePolicy(allowedAttributes);

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, ReturnAllowedAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
