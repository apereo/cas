package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
public class RegexMatchingRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexMatchingRegisteredServiceProxyPolicy.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifySerializeARegexMatchingRegisteredServiceProxyPolicyToJson() throws IOException {
        final RegexMatchingRegisteredServiceProxyPolicy policyWritten = new RegexMatchingRegisteredServiceProxyPolicy("pattern");

        MAPPER.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceProxyPolicy policyRead = MAPPER.readValue(JSON_FILE, RegexMatchingRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}
