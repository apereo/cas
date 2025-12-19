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
 * This is {@link PseudonymousAccessAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata55"
})
class PseudonymousAccessAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "PseudonymousAccessAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyMatch() throws Throwable {
        val filter = new PseudonymousAccessAttributeReleasePolicy();
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("schacHomeOrganization", "apereo",
                    "eduPersonScopedAffiliation", "cas@apereo.org",
                    "eduPersonAssurance", "gold")))
            .build();
        val attributes = filter.getAttributes(context);
        assertEquals(3, attributes.size());
        assertTrue(attributes.containsKey("schacHomeOrganization"));
        assertTrue(attributes.containsKey("eduPersonScopedAffiliation"));
        assertTrue(attributes.containsKey("eduPersonAssurance"));
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new PseudonymousAccessAttributeReleasePolicy();
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, PseudonymousAccessAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }
}

