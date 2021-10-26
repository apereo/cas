package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Groovy")
public class GroovyScriptAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "groovyScriptAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

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

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("likes"));
        assertTrue(attributes.containsKey("id"));
        assertTrue(attributes.containsKey("another"));
    }

    @Test
    public void verifyFails() {
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("classpath:bad-path.groovy");
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.isEmpty());
    }


    @Test
    public void verifySystemPropertyInRef() throws Exception {
        val file = File.createTempFile("GroovyAttributeRelease", ".groovy");
        try (val is = new ClassPathResource("GroovyAttributeRelease.groovy").getInputStream()) {
            is.transferTo(new FileOutputStream(file));
        }
        assertTrue(file.exists());
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("file:${#systemProperties['java.io.tmpdir']}/" + file.getName());
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("likes"));
        assertTrue(attributes.containsKey("id"));
        assertTrue(attributes.containsKey("another"));
    }
}
