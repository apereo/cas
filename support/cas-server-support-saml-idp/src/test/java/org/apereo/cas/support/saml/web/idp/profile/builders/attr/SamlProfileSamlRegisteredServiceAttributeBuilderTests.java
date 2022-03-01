package org.apereo.cas.support.saml.web.idp.profile.builders.attr;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileBuilderContext;
import org.apereo.cas.support.saml.web.idp.profile.builders.SamlProfileObjectBuilder;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlProfileSamlRegisteredServiceAttributeBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("SAML2")
public class SamlProfileSamlRegisteredServiceAttributeBuilderTests extends BaseSamlIdPConfigurationTests {

    @Autowired
    @Qualifier("samlProfileSamlAttributeStatementBuilder")
    private SamlProfileObjectBuilder<AttributeStatement> samlProfileSamlAttributeStatementBuilder;

    @Test
    public void verifyNoEncryption() throws Exception {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);

        val service2 = getSamlRegisteredServiceFor(UUID.randomUUID().toString());
        service2.setMetadataLocation("classpath:/unknown.xml");
        service2.setEncryptionDataAlgorithms(null);
        service2.setEncryptionKeyAlgorithms(null);
        service2.setEncryptAttributes(true);
        service2.setEncryptionOptional(true);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(getAssertion())
            .registeredService(service2)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);
        assertTrue(statement.getEncryptedAttributes().isEmpty());
        assertFalse(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionDisabledIfAssertionEncrypted() throws Exception {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);
        service.setEncryptAssertions(true);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();

        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(getAssertion())
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);

        assertTrue(statement.getEncryptedAttributes().isEmpty());
        assertFalse(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForAllUndefined() throws Exception {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade
            .get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(getAssertion())
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertTrue(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForAll() throws Exception {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);
        service.getEncryptableAttributes().add("*");

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(getAssertion())
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertTrue(statement.getAttributes().isEmpty());
    }

    @Test
    public void verifyEncryptionForSome() throws Exception {
        val service = getSamlRegisteredServiceForTestShib();
        service.setEncryptAttributes(true);
        service.getEncryptableAttributes().add("uid");

        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, service.getServiceId()).get();
        val buildContext = SamlProfileBuilderContext.builder()
            .samlRequest(getAuthnRequestFor(service))
            .httpRequest(new MockHttpServletRequest())
            .httpResponse(new MockHttpServletResponse())
            .authenticatedAssertion(getAssertion())
            .registeredService(service)
            .adaptor(adaptor)
            .binding(SAMLConstants.SAML2_POST_BINDING_URI)
            .build();

        val statement = samlProfileSamlAttributeStatementBuilder.build(buildContext);

        assertFalse(statement.getEncryptedAttributes().isEmpty());
        assertFalse(statement.getAttributes().isEmpty());
    }
}
