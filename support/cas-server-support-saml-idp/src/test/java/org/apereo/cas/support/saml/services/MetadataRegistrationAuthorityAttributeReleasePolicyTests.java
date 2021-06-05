package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MetadataRegistrationAuthorityAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata3"
})
public class MetadataRegistrationAuthorityAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "MetadataRegistrationAuthority.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifyNoMatch() {
        val filter = new MetadataRegistrationAuthorityAttributeReleasePolicy();
        filter.setRegistrationAuthority("^nothing.+");
        filter.setAllowedAttributes(List.of("sn"));

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val attributes = filter.getAttributes(
            CoreAuthenticationTestUtils.getPrincipal("casuser", CollectionUtils.wrap("sn", "surname")),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void verifyMatch() {
        val filter = new MetadataRegistrationAuthorityAttributeReleasePolicy();
        filter.setRegistrationAuthority("urn:mace:.+");
        filter.setAllowedAttributes(List.of("sn"));

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val attributes = filter.getAttributes(
            CoreAuthenticationTestUtils.getPrincipal("casuser",
                CollectionUtils.wrap("eduPersonPrincipalName", "cas-eduPerson-user",
                    "mail", "cas@example.org",
                    "sn", "surname")),
            CoreAuthenticationTestUtils.getService(), registeredService);
        assertFalse(attributes.isEmpty());
        assertFalse(attributes.containsKey("eduPersonPrincipalName"));
        assertFalse(attributes.containsKey("mail"));
        assertTrue(attributes.containsKey("sn"));
    }

    @Test
    public void verifySerializationToJson() throws IOException {
        val filter = new MetadataRegistrationAuthorityAttributeReleasePolicy();
        filter.setRegistrationAuthority("urn:mace:.+");
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, MetadataRegistrationAuthorityAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
    }
}
