package org.apereo.cas.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
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
public class PatternMatchingAttributeReleasePolicyTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(),
        "PatternMatchingAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializeToJson() throws IOException {
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
    public void verfyPatternTransform() throws IOException {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${1}@${2}/${3}"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
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
    public void verfyTransformEntireMatch() throws IOException {
        val policy = new PatternMatchingAttributeReleasePolicy();
        policy.getAllowedAttributes().put("memberOf",
            new PatternMatchingAttributeReleasePolicy.Rule()
                .setPattern("^CN=(\\w+),\\s*OU=(\\w+),\\s*DC=(\\w+)")
                .setTransform("${0}/org"));

        val releasePolicyContext = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(CoreAuthenticationTestUtils.getRegisteredService())
            .service(CoreAuthenticationTestUtils.getService())
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
