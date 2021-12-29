package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MetadataEntityAttributesAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata2"
})
public class MetadataEntityAttributesAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "MetadataEntityAttributesAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializationToJson() throws IOException {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeFormat("entity-format");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, MetadataEntityAttributesAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    public void verifyPredicateFails() {
        val filter = new MetadataEntityAttributesAttributeReleasePolicy();
        filter.setEntityAttribute("entity-attribute");
        filter.setEntityAttributeFormat("entity-format");
        filter.setEntityAttributeValues(CollectionUtils.wrapSet("one", "two"));

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("givenName", UUID.randomUUID().toString())))
            .build();
        val attributes = filter.getAttributes(context);
        assertTrue(attributes.isEmpty());
    }
}
