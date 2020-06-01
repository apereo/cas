package org.apereo.cas.support.saml.web.idp.profile.builders.authn;

import org.apereo.cas.support.saml.BaseSamlIdPConfigurationTests;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultAuthnContextClassRefBuilderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class DefaultAuthnContextClassRefBuilderTests extends BaseSamlIdPConfigurationTests {
    @Test
    public void verifyOperationByService() {
        val builder = new DefaultAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        service.setRequiredAuthenticationContextClass("some-context-class");
        val authnRequest = getAuthnRequestFor(service);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val result = builder.build(getAssertion(), authnRequest, adaptor.get(), service);
        assertEquals(service.getRequiredAuthenticationContextClass(), result);
    }

    @Test
    public void verifyOperationByAuthnRequest() {
        val builder = new DefaultAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);
        val context = mock(RequestedAuthnContext.class);
        when(context.getAuthnContextClassRefs()).thenReturn(List.of());
        when(authnRequest.getRequestedAuthnContext()).thenReturn(context);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val result = builder.build(getAssertion(), authnRequest, adaptor.get(), service);
        assertEquals(AuthnContext.PPT_AUTHN_CTX, result);
    }

    @Test
    public void verifyOperationByAssertion() {
        val builder = new DefaultAuthnContextClassRefBuilder(casProperties);
        val service = getSamlRegisteredServiceForTestShib();
        val authnRequest = getAuthnRequestFor(service);

        val classRef = mock(AuthnContextClassRef.class);
        when(classRef.getURI()).thenReturn("some-context");
        val context = mock(RequestedAuthnContext.class);
        when(context.getAuthnContextClassRefs()).thenReturn(List.of(classRef));
        when(authnRequest.getRequestedAuthnContext()).thenReturn(context);
        val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(samlRegisteredServiceCachingMetadataResolver, service, authnRequest);
        val result = builder.build(getAssertion(), authnRequest, adaptor.get(), service);
        assertEquals(AuthnContext.PPT_AUTHN_CTX, result);
    }
}
