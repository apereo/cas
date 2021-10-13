package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
@TestPropertySource(properties = "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-definitions.json")
public class SamlProfileSamlAttributeStatementBuilderTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    @Test
    public void verifyAttributeAsNameIDPersistent() {
        val service = getSamlRegisteredServiceForTestShib();
        service.getAttributeValueTypes().put("customNameId", NameIDType.PERSISTENT);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            getAssertion(Map.of("customNameId", List.of(UUID.randomUUID().toString()))),
            service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        val result = attributes.stream().filter(a -> a.getName().equals("customNameId")).findFirst();
        assertTrue(result.isPresent());
        assertTrue(result.get().getAttributeValues().get(0) instanceof NameIDType);
    }

    @Test
    public void verifyAttributeAsNameIDSameAsSubject() {
        val service = getSamlRegisteredServiceForTestShib();
        service.getAttributeValueTypes().put("customNameId", NameIDType.class.getSimpleName());
        
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            getAssertion(Map.of("customNameId", List.of(UUID.randomUUID().toString()))),
            service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        val result = attributes.stream().filter(a -> a.getName().equals("customNameId")).findFirst();
        assertTrue(result.isPresent());
        assertTrue(result.get().getAttributeValues().get(0) instanceof NameIDType);
    }

    @Test
    public void verifyTestAttributeDefns() {
        val service = getSamlRegisteredServiceForTestShib();

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(), getAssertion(Map.of("emptyAttributeCol", List.of())),
            service, adaptor, SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.stream().anyMatch(a -> a.getName().equals("urn:oid:0.9.2342.19200300.100.1.3")));
        assertTrue(attributes.stream().anyMatch(a -> a.getName().equals("alias")));
        assertTrue(attributes.stream().anyMatch(a -> a.getName().equals("common-name")));
        assertTrue(attributes.stream().anyMatch(a -> a.getName().equals("nickname")));
    }

    @Test
    public void verifyFriendlyNamesForKnownAttributes() {
        val service = getSamlRegisteredServiceForTestShib();
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val statement = samlProfileSamlAttributeStatementBuilder.build(getAuthnRequestFor(service), new MockHttpServletRequest(),
            new MockHttpServletResponse(),
            getAssertion(Map.of("urn:oid:0.9.2342.19200300.100.1.1", "casuser",
                "urn:oid:2.5.4.20", "+13477465341",
                "urn:oid:1.3.6.1.4.1.5923.1.1.1.6", "casuser-principal",
                "urn:oid:0.9.2342.19200300.100.1.3", "cas@example.org")),
            service,
            adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());

        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.stream()
            .anyMatch(a -> a.getName().equals("urn:oid:0.9.2342.19200300.100.1.1") && a.getFriendlyName().equalsIgnoreCase("uid")));
        assertTrue(attributes.stream()
            .anyMatch(a -> a.getName().equals("urn:oid:2.5.4.20") && a.getFriendlyName().equalsIgnoreCase("telephoneNumber")));
        assertTrue(attributes.stream()
            .anyMatch(a -> a.getName().equals("urn:oid:1.3.6.1.4.1.5923.1.1.1.6") && a.getFriendlyName().equalsIgnoreCase("eduPersonPrincipalName")));
        assertTrue(attributes.stream()
            .anyMatch(a -> a.getName().equals("urn:oid:0.9.2342.19200300.100.1.3") && a.getFriendlyName().equalsIgnoreCase("email")));
    }
}
