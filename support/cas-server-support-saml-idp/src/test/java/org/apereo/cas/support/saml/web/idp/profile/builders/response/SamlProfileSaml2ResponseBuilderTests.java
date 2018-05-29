package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.client.validation.Assertion;
import org.junit.Test;
import org.opensaml.messaging.context.MessageContext;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.*;

/**
 * This is {@link SamlProfileSaml2ResponseBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class SamlProfileSaml2ResponseBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifySamlResponseAllSigned() {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        final var service = getSamlRegisteredServiceForTestShib(true, true);
        final var adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final var authnRequest = getAuthnRequestFor(service);
        final var assertion = getAssertion();

        final var samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAllSignedEncrypted() {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        final var service = getSamlRegisteredServiceForTestShib(true, true, true);
        final var adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final var authnRequest = getAuthnRequestFor(service);
        final var assertion = getAssertion();

        final var samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAssertionSigned() {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        final var service = getSamlRegisteredServiceForTestShib(false, true);
        final var adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final var authnRequest = getAuthnRequestFor(service);
        final var assertion = getAssertion();

        final var samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseResponseSigned() {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        final var service = getSamlRegisteredServiceForTestShib(true, false);
        final var adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final var authnRequest = getAuthnRequestFor(service);
        final var assertion = getAssertion();

        final var samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseNothingSigned() {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();

        final var service = getSamlRegisteredServiceForTestShib(false, false);
        final var adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final var authnRequest = getAuthnRequestFor(service);
        final var assertion = getAssertion();

        final var samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }
}
