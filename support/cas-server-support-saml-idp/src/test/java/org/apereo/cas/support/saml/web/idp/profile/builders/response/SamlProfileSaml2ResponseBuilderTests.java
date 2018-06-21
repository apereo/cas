package org.apereo.cas.support.saml.web.idp.profile.builders.response;

import org.apereo.cas.category.FileSystemCategory;
import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.jasig.cas.client.validation.Assertion;
import org.junit.Test;
import org.junit.experimental.categories.Category;
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
@Category(FileSystemCategory.class)
public class SamlProfileSaml2ResponseBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifySamlResponseAllSigned() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final SamlRegisteredService service = getSamlRegisteredServiceForTestShib(true, true);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final AuthnRequest authnRequest = getAuthnRequestFor(service);
        final Assertion assertion = getAssertion();

        final Response samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAllSignedEncrypted() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final SamlRegisteredService service = getSamlRegisteredServiceForTestShib(true, true, true);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final AuthnRequest authnRequest = getAuthnRequestFor(service);
        final Assertion assertion = getAssertion();

        final Response samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseAssertionSigned() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final SamlRegisteredService service = getSamlRegisteredServiceForTestShib(false, true);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final AuthnRequest authnRequest = getAuthnRequestFor(service);
        final Assertion assertion = getAssertion();

        final Response samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseResponseSigned() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final SamlRegisteredService service = getSamlRegisteredServiceForTestShib(true, false);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final AuthnRequest authnRequest = getAuthnRequestFor(service);
        final Assertion assertion = getAssertion();

        final Response samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }

    @Test
    public void verifySamlResponseNothingSigned() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();

        final SamlRegisteredService service = getSamlRegisteredServiceForTestShib(false, false);
        final SamlRegisteredServiceServiceProviderMetadataFacade adaptor =
            SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver,
                service, service.getServiceId()).get();

        final AuthnRequest authnRequest = getAuthnRequestFor(service);
        final Assertion assertion = getAssertion();

        final Response samlResponse = samlProfileSamlResponseBuilder.build(authnRequest, request, response,
            assertion, service, adaptor,
            SAMLConstants.SAML2_POST_BINDING_URI,
            new MessageContext());
        assertNotNull(samlResponse);
    }
}
