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
 * This is {@link PersonalizedAccessAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata155"
})
class PersonalizedAccessAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "PersonalizedAccessAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    void verifyMatch() throws Throwable {
        val filter = new PersonalizedAccessAttributeReleasePolicy();
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .service(CoreAuthenticationTestUtils.getService())
            .applicationContext(applicationContext)
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap(
                    "schacHomeOrganization", "apereo",
                    "sn", "CAS",
                    "mail", "cas@apereo.org",
                    "eduPersonScopedAffiliation", "cas@apereo.org",
                    "eduPersonAssurance", "gold")))
            .build();
        val attributes = filter.getAttributes(context);
        assertEquals(5, attributes.size());
    }

    @Test
    void verifySerializationToJson() throws IOException {
        val filter = new PersonalizedAccessAttributeReleasePolicy();
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, PersonalizedAccessAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }
}

