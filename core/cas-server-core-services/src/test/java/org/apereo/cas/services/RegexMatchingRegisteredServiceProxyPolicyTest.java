package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RegexMatchingRegisteredServiceProxyPolicyTest {

    private static final File JSON_FILE = new File("regexMatchingRegisteredServiceProxyPolicy.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void verifySerializeARegexMatchingRegisteredServiceProxyPolicyToJson() throws IOException {
        final RegexMatchingRegisteredServiceProxyPolicy policyWritten = new RegexMatchingRegisteredServiceProxyPolicy("pattern");

        mapper.writeValue(JSON_FILE, policyWritten);

        final RegisteredServiceProxyPolicy policyRead = mapper.readValue(JSON_FILE, RegexMatchingRegisteredServiceProxyPolicy.class);

        assertEquals(policyWritten, policyRead);
    }
}