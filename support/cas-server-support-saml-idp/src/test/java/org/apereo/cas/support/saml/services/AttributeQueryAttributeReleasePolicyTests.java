package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AttributeQueryAttributeReleasePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAMLAttributes")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.entity-id=https://cas.example.org/idp",
    "cas.authn.saml-idp.metadata.file-system.location=${#systemProperties['java.io.tmpdir']}/idp-metadata14"
})
class AttributeQueryAttributeReleasePolicyTests extends BaseSamlIdPConfigurationTests {
    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "AttributeQueryAttributeReleasePolicyTests.json");

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    @Autowired
    @Qualifier("samlAttributeQueryTicketFactory")
    private SamlAttributeQueryTicketFactory samlAttributeQueryTicketFactory;

    @Test
    void verifySerializationToJson() {
        val filter = new AttributeQueryAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("a", "b"));
        MAPPER.writeValue(JSON_FILE, filter);
        val strategyRead = MAPPER.readValue(JSON_FILE, AttributeQueryAttributeReleasePolicy.class);
        assertEquals(filter, strategyRead);
        assertNotNull(strategyRead.toString());
    }

    @Test
    void verifyReleasesAttributes() throws Throwable {
        val filter = new AttributeQueryAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "cn"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        val service = CoreAuthenticationTestUtils.getService("https://sp.testshib.org/shibboleth-sp");
        service.getAttributes().put("owner", List.of(SamlAttributeQueryTicket.class.getName()));
        val principal = CoreAuthenticationTestUtils.getPrincipal("casuser");

        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(service)
            .principal(principal)
            .build();
        assertTrue(filter.supports(context));
        val attributes = filter.getAttributes(context);
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("cn"));
    }

    /**
     * An attribute query arrives over SOAP with no entity id in the request, so the service of the attribute query
     * ticket is all the policy has to find the metadata of the relying party.
     */
    @Test
    void verifyReleasesAttributesForAttributeQueryTicketService() throws Throwable {
        val filter = new AttributeQueryAttributeReleasePolicy();
        filter.setAllowedAttributes(CollectionUtils.wrapList("uid", "cn"));
        val registeredService = SamlIdPTestUtils.getSamlRegisteredService();
        registeredService.setAttributeReleasePolicy(filter);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));

        val ticket = samlAttributeQueryTicketFactory.create(UUID.randomUUID().toString(),
            SamlUtils.newSamlObject(AttributeQuery.class), registeredService.getServiceId(),
            new MockTicketGrantingTicket("casuser"));
        val context = RegisteredServiceAttributeReleasePolicyContext.builder()
            .registeredService(registeredService)
            .applicationContext(applicationContext)
            .service(ticket.getService())
            .principal(CoreAuthenticationTestUtils.getPrincipal("casuser"))
            .build();
        assertTrue(filter.supports(context));
        val attributes = filter.getAttributes(context);
        assertTrue(attributes.containsKey("uid"));
        assertTrue(attributes.containsKey("cn"));
    }
}
