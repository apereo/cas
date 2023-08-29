package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("GroovyServices")
class GroovyScriptAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "groovyScriptAttributeReleasePolicy.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializeAGroovyScriptAttributeReleasePolicyToJson() throws IOException {
        val policyWritten = new GroovyScriptAttributeReleasePolicy();
        MAPPER.writeValue(JSON_FILE, policyWritten);
        val policyRead = MAPPER.readValue(JSON_FILE, GroovyScriptAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyAction() throws Throwable {
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("classpath:GroovyAttributeRelease.groovy");

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("likes"));
        assertTrue(attributes.containsKey("id"));
        assertTrue(attributes.containsKey("another"));
    }

    @Test
    void verifyFails() throws Throwable {
        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("classpath:bad-path.groovy");
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.isEmpty());
    }


    @Test
    void verifySystemPropertyInRef() throws Throwable {
        val file = File.createTempFile("GroovyAttributeRelease", ".groovy");
        try (val is = new ClassPathResource("GroovyAttributeRelease.groovy").getInputStream()) {
            is.transferTo(new FileOutputStream(file));
        }
        assertTrue(file.exists());

        val applicationContext = new StaticApplicationContext();
        applicationContext.refresh();
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("file:${#systemProperties['java.io.tmpdir']}/" + file.getName());
        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal())
            .applicationContext(applicationContext)
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("username"));
        assertTrue(attributes.containsKey("likes"));
        assertTrue(attributes.containsKey("id"));
        assertTrue(attributes.containsKey("another"));
    }
}
