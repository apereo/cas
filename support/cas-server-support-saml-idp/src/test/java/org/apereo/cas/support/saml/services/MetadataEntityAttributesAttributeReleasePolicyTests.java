package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MetadataEntityAttributesAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata222"
})
class MetadataEntityAttributesAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "MetadataEntityAttributesAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeFormat("entity-format");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));
        filter.setEntityIds(CollectionUtils.wrapSet("^https://sp\\.example\\.org/.+"));
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, MetadataEntityAttributesAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    void verifyNullEntityIdsAreEmpty() {
        val json = """
            {
              "@class": "org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy",
              "entityAttribute": "entity-attribute",
              "entityIds": null
            }
            """;
        val policy = MAPPER.readValue(json, MetadataEntityAttributesAttributeReleasePolicy.class);
        assertNotNull(policy.getEntityIds());
        assertTrue(policy.getEntityIds().isEmpty());
    }

    @Test
    void verifyPredicateFails() throws Throwable {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeFormat("entity-format");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));
        val attributes = filter.getAttributes(buildContext(filter));
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyEntityAttributeMatches() throws Throwable {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("http://macedir.org/entity-category");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("http://refeds.org/category/research-and-scholarship"));
        filter.setEntityIds(CollectionUtils.wrapSet("^https://unknown\\.example\\.org"));
        filter.setAllowedAttributes(CollectionUtils.wrapList("givenName"));
        val attributes = filter.getAttributes(buildContext(filter));
        assertTrue(attributes.containsKey("givenName"));
    }

    @Test
    void verifyEntityIdMatches() throws Throwable {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));
        filter.setEntityIds(CollectionUtils.wrapSet("^https://unknown\\.example\\.org", "^HTTPS://sp\\.testshib\\.org/.+"));
        filter.setAllowedAttributes(CollectionUtils.wrapList("givenName"));
        val attributes = filter.getAttributes(buildContext(filter));
        assertTrue(attributes.containsKey("givenName"));
    }

    @Test
    void verifyEntityIdDoesNotMatch() throws Throwable {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));
        filter.setEntityIds(CollectionUtils.wrapSet("^https://unknown\\.example\\.org", "testshib\\.org/unknown"));
        filter.setAllowedAttributes(CollectionUtils.wrapList("givenName"));
        val attributes = filter.getAttributes(buildContext(filter));
        assertTrue(attributes.isEmpty());
    }

    private RegisteredServiceAttributeReleasePolicyContext buildContext(final MetadataEntityAttributesAttributeReleasePolicy filter) {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        return RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("givenName", UUID.randomUUID().toString())))
            .build();
    }
}
