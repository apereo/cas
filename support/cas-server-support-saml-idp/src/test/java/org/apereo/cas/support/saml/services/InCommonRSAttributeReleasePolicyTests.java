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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link InCommonRSAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata12"
})
class InCommonRSAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "InCommonRSAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyMatch() throws Throwable {
        val filter = new InCommonRSAttributeReleasePolicy();
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "cas-eduPerson-user",
                    "mail", "cas@example.org",
                    "sn", "surname")))
            .build();
        val attributes = filter.getAttributes(context);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("eduPersonPrincipalName"));
        assertTrue(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("sn"));
    }

    @Test
    void verifyOids() throws Throwable {
        val filter = new InCommonRSAttributeReleasePolicy();
        filter.setUseUniformResourceName(true);

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "cas-eduPerson-user",
                    "mail", "cas@example.org",
                    "sn", "surname")))
            .build();
        val attributes = filter.getAttributes(context);
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.containsKey("urn:oid:1.3.6.1.4.1.5923.1.1.1.6"));
        assertTrue(attributes.containsKey("urn:oid:0.9.2342.19200300.100.1.3"));
        assertTrue(attributes.containsKey("urn:oid:2.5.4.4"));
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new InCommonRSAttributeReleasePolicy();
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, InCommonRSAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }

    @Test
    void verifyAttributeDefinitions() {
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        val policy = new InCommonRSAttributeReleasePolicy();
        policy.setUseUniformResourceName(true);

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp"))
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        var definitions = policy.determineRequestedAttributeDefinitions(context);
        assertTrue(definitions.containsAll(InCommonRSAttributeReleasePolicy.ALLOWED_ATTRIBUTES.values()));

        policy.setUseUniformResourceName(false);
        definitions = policy.determineRequestedAttributeDefinitions(context);
        assertTrue(definitions.containsAll(InCommonRSAttributeReleasePolicy.ALLOWED_ATTRIBUTES.keySet()));
    }
}

