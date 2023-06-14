package org.apereo.cas.services;

import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

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
class RegexMatchingRegisteredServiceProxyPolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "regexMatchingRegisteredServiceProxyPolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeARegexMatchingRegisteredServiceProxyPolicyToJson() throws Exception {
        val policy = new RegexMatchingRegisteredServiceProxyPolicy();
        policy.setPattern("pattern");
        policy.setExactMatch(true);
        policy.setUseServiceId(true);
        MAPPER.writeValue(JSON_FILE, policy);
        val policyRead = MAPPER.readValue(JSON_FILE, RegexMatchingRegisteredServiceProxyPolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    public void verifyBadPattern() throws Exception {
        val policy = new RegexMatchingRegisteredServiceProxyPolicy();
        policy.setPattern("***");
        assertFalse(policy.isAllowedProxyCallbackUrl(RegisteredServiceTestUtils.getRegisteredService(),
            new URL("https://github.com/apereo/cas")));
    }

    @Test
    public void verifyExactMatch() throws Exception {
        val policy = new RegexMatchingRegisteredServiceProxyPolicy();
        policy.setPattern("https://github.com/apereo/cas");
        policy.setExactMatch(true);
        assertTrue(policy.isAllowedProxyCallbackUrl(RegisteredServiceTestUtils.getRegisteredService(),
            new URL("https://github.com/apereo/cas")));
    }

    @Test
    public void verifyServiceIdPattern() throws Exception {
        val policy = new RegexMatchingRegisteredServiceProxyPolicy();
        policy.setUseServiceId(true);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService("^https:.+/apereo/cas");
        assertTrue(policy.isAllowedProxyCallbackUrl(registeredService, new URL("https://github.com/apereo/cas")));
    }
}
