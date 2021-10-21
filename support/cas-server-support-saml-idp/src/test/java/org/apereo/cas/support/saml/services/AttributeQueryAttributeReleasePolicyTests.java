package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
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
 * This is {@link AttributeQueryAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAML")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata4"
})
public class AttributeQueryAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AttributeQueryAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Test
    public void verifySerializationToJson() throws IOException {
        val filter = new AttributeQueryAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("a", "b"));
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, AttributeQueryAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    public void verifyReleasesAttributes() {
        val filter = new AttributeQueryAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "cn"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val service = CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp");
        service.getAttributes().put("owner", List.of(SamlAttributeQueryTicket.class.getName()));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");
        assertTrue(filter.supports(principal, service, registeredService));
        val attributes = filter.getAttributes(principal, service, registeredService);
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("cn"));
    }
}
