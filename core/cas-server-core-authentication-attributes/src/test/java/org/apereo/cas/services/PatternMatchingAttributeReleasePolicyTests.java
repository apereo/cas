package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PatternMatchingAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("Attributes")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
class PatternMatchingAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "PatternMatchingAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifySerializeToJson() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        assertNotNull(policy.getName());
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("CN=(\\w+),OU=(\\w+)")
                .setTransform("${1}/${2}"));
        MAPPER.writeValue(JSON_FILE, policy);
        val policyRead = MAPPER.readValue(JSON_FILE, PatternMatchingAttributeReleasePolicy.class);
        assertEquals(policy, policyRead);
    }

    @Test
    void verifyPatternTransform() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${1}@${2}/${3}"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org", "CN=g2,OU=example,DC=org"),
                    "another", List.of("CN=g3", "CN=g4"))))
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("memberOf"));
        assertFalse(attributes.containsKey("another"));

        val values = attributes.get("memberOf");
        assertTrue(values.contains("g1@example/org"));
        assertTrue(values.contains("g2@example/org"));
    }

    @Test
    void verifyTransformEntireMatch() throws Throwable {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${0}/org"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal(
                Map.of("memberOf", List.of("CN=g1,OU=example,DC=org"),
                    "another", List.of("CN=g3", "CN=g4"))))
            .build();
        val attributes = policy.getAttributes(releasePolicyContext);
        assertTrue(attributes.containsKey("memberOf"));
        assertFalse(attributes.containsKey("another"));

        val values = attributes.get("memberOf");
        assertTrue(values.contains("CN=g1,OU=example,DC=org/org"));
    }

}
