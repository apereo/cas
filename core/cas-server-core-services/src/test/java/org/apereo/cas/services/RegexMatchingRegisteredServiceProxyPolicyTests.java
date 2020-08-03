package org.apereo.cas.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.0.0
 */
@Tag("RegisteredService")
public class RegexMatchingRegisteredServiceProxyPolicyTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexMatchingRegisteredServiceProxyPolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeARegexMatchingRegisteredServiceProxyPolicyToJson() throws Exception {
        val policyWritten = new RegexMatchingRegisteredServiceProxyPolicy("pattern");
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, RegexMatchingRegisteredServiceProxyPolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyBadPattern() throws Exception {
        val policy = new RegexMatchingRegisteredServiceProxyPolicy("***");
        assertFalse(policy.isAllowedProxyCallbackUrl(new URL("https://github.com/apereo/cas")));
    }
}
