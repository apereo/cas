package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.core.xml.schema.XSAny;
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
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlAttributeStatementBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAMLResponse")
@TestPropertySource(properties = {
    "cas.authn.saml-idp.core.context.authentication-context-class-mappings=http://schemas.microsoft.com/claims/multipleauthn->mfa-dummy",
    "cas.authn.attribute-repository.attribute-definition-store.json.location=classpath:/basic-definitions.json"
})
class SamlProfileSamlAttributeStatementBuilderTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    @Test
    void verifyAttributeAsNameIDPersistent() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.getAttributeValueTypes().put("customNameId", NameIDType.PERSISTENT);

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(Map.of("customNameId", List.of(UUID.randomUUID().toString())))))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);

        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        val result = attributes.stream().filter(a -> "customNameId".equals(a.getName())).findFirst();
        assertTrue(result.isPresent());
        assertInstanceOf(NameIDType.class, result.get().getAttributeValues().getFirst());
    }

    @Test
    void verifyAttributeAsNameIDSameAsSubject() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        service.getAttributeValueTypes().put("customNameId", NameIDType.class.getSimpleName());

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(Map.of("customNameId", List.of(UUID.randomUUID().toString())))))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);
        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        val result = attributes.stream().filter(a -> "customNameId".equals(a.getName())).findFirst();
        assertTrue(result.isPresent());
        assertInstanceOf(NameIDType.class, result.get().getAttributeValues().getFirst());
    }

    @Test
    void verifyTestAttributeDefns() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();

        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(Map.of("emptyAttributeCol", List.of()))))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);
        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());
        assertTrue(attributes.stream().anyMatch(a -> "urn:oid:0.9.2342.19200300.100.1.3".equals(a.getName())));
        assertTrue(attributes.stream().anyMatch(a -> "alias".equals(a.getName())));
        assertTrue(attributes.stream().anyMatch(a -> "common-name".equals(a.getName())));
        assertTrue(attributes.stream().anyMatch(a -> "nickname".equals(a.getName())));
    }

    @Test
    void verifyFriendlyNamesForKnownAttributes() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).orElseThrow();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(Map.of("urn:oid:0.9.2342.19200300.100.1.1", "casuser",
                "urn:oid:2.5.4.20", "+13477465341",
                "urn:oid:1.3.6.1.4.1.5923.1.1.1.6", "casuser-principal",
                "urn:oid:0.9.2342.19200300.100.1.3", "cas@example.org"))))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);
        val attributes = statement.getAttributes();
        assertFalse(attributes.isEmpty());

        assertTrue(attributes.stream()
            .anyMatch(a -> "urn:oid:0.9.2342.19200300.100.1.1".equals(a.getName())
                && "uid".equalsIgnoreCase(a.getFriendlyName())));
        assertTrue(attributes.stream()
            .anyMatch(a -> "urn:oid:2.5.4.20".equals(a.getName())
                && "telephoneNumber".equalsIgnoreCase(a.getFriendlyName())));
        assertTrue(attributes.stream()
            .anyMatch(a -> "urn:oid:1.3.6.1.4.1.5923.1.1.1.6".equals(a.getName())
                && "eduPersonPrincipalName-FriendlyName".equalsIgnoreCase(a.getFriendlyName())));
        assertTrue(attributes.stream()
            .anyMatch(a -> "urn:oid:0.9.2342.19200300.100.1.3".equals(a.getName())
                && "email".equalsIgnoreCase(a.getFriendlyName())));
    }

    @Test
    void verifyAuthnContextAsAttribute() throws Throwable {
        val service = getSamlRegisteredServiceForTestShib();
        val adaptor = SamlRegisteredServiceMetadataAdaptor
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).orElseThrow();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(Optional.of(getAssertion(
                Map.of(casProperties.getAuthn().getMfa().getCore().getAuthenticationContextAttribute(),
                    List.of(TestMultifactorAuthenticationProvider.ID)))))
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);
        val attributes = statement.getAttributes();
        val values = attributes.stream()
            .filter(attr -> "http://schemas.microsoft.com/claims/authnmethodsreferences".equals(attr.getName()))
            .findFirst().orElseThrow();
        assertFalse(values.getAttributeValues().isEmpty());
        val value = (XSAny) values.getAttributeValues().getFirst();
        assertEquals("http://schemas.microsoft.com/claims/multipleauthn", value.getTextContent());
    }
}
