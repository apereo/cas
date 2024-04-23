package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("GroovyServices")
@SpringBootTest(classes = {
    CasCoreUtilAutoConfiguration.class,
    RefreshAutoConfiguration.class
})
@EnableConfigurationProperties(CasConfigurationProperties.class)
class GroovyScriptAttributeReleasePolicyTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySerializeAGroovyScriptAttributeReleasePolicyToJson() throws IOException {
        val jsonFile = Files.createTempFile(RandomUtils.randomAlphabetic(8), ".json").toFile();
        val policyWritten = new GroovyScriptAttributeReleasePolicy();
        MAPPER.writeValue(jsonFile, policyWritten);
        val policyRead = MAPPER.readValue(jsonFile, GroovyScriptAttributeReleasePolicy.class);
        assertEquals(policyWritten, policyRead);
    }

    @Test
    void verifyAction() throws Throwable {
        val policy = new GroovyScriptAttributeReleasePolicy();
        policy.setGroovyScript("classpath:GroovyAttributeRelease.groovy");

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
