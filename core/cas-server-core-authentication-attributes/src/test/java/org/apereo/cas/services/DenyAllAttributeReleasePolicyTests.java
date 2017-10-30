package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DenyAllAttributeReleasePolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "denyAllAttributeReleasePolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeADenyAllAttributeReleasePolicyToJson() throws IOException {
        final DenyAllAttributeReleasePolicy policyWritten = new DenyAllAttributeReleasePolicy();

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceAttributeReleasePolicy policyRead = MAPPER.readValue(JSON_FILE, DenyAllAttributeReleasePolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
