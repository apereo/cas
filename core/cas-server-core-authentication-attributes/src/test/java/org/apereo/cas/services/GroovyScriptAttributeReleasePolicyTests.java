package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public class GroovyScriptAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "groovyScriptAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Test
    public void verifySerializeAGroovyScriptAttributeReleasePolicyToJson() throws IOException {
        val policyWritten = new GroovyScriptAttributeReleasePolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, GroovyScriptAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    public void verifyAction() {
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("classpath:GroovyAttributeRelease.groovy");
        val attributes = policy.getAttributes(CoreAuthenticationTestUtils.getPrincipal(), CoreAuthenticationTestUtils.getService(),
            CoreAuthenticationTestUtils.getRegisteredService());
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("likes"));
        assertTrue(attributes.containsKey("id"));
        assertTrue(attributes.containsKey("another"));
    }
}
