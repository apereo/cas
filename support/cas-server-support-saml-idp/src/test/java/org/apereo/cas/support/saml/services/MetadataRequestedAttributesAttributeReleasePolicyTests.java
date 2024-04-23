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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MetadataRequestedAttributesAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata83"
})
class MetadataRequestedAttributesAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "MetadataRequestedAttributesAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyNoServiceOrEntityId() throws Throwable {
        val filter = new MetadataRequestedAttributesAttributeReleasePolicy();
        filter.setUseFriendlyName(true);
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("eduPersonPrincipalName", "cas-eduPerson-user"));
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val attributes = filter.getAttributes(context);
        assertTrue(attributes.isEmpty());
    }

    @Test
    void verifyMatch() throws Throwable {
        val filter = new MetadataRequestedAttributesAttributeReleasePolicy();
        filter.setUseFriendlyName(true);
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser",
            CollectionUtils.wrap("eduPersonPrincipalName", "cas-eduPerson-user"));
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(principal)
            .build();
        val attributes = filter.getAttributes(context);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("eduPersonPrincipalName"));
        val defns = filter.determineRequestedAttributeDefinitions(context);
        assertEquals(3, defns.size());
        assertTrue(defns.contains("eduPersonPrincipalName"));
    }

    @Test
    void verifySerializationToJson() throws Throwable {
        val filter = new MetadataRequestedAttributesAttributeReleasePolicy();
        filter.setUseFriendlyName(true);
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, MetadataRequestedAttributesAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }
}
