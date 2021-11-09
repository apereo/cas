package org.apereo.cas.web;

import org.apereo.cas.web.flow.BaseSamlIdPWebflowTests;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link SamlIdPSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("SAML")
public class SamlIdPSingleSignOnParticipationStrategyTests extends BaseSamlIdPWebflowTests {
    @Autowired
    @Qualifier("samlIdPSingleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy samlIdPSingleSignOnParticipationStrategy;

    @Test
    public void verifyParticipation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val issuer = UUID.randomUUID().toString();
        val authnRequest = getAuthnRequestFor(issuer);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build()
            .attribute(AuthnRequest.class.getName(), authnRequest)
            .attribute(Issuer.class.getName(), issuer);

        assertTrue(samlIdPSingleSignOnParticipationStrategy.supports(ssoRequest));
        assertTrue(samlIdPSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyForcedAuthn() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val issuer = UUID.randomUUID().toString();
        val authnRequest = getAuthnRequestFor(issuer);
        when(authnRequest.isForceAuthn()).thenReturn(Boolean.TRUE);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build()
            .attribute(AuthnRequest.class.getName(), authnRequest)
            .attribute(Issuer.class.getName(), issuer);
        assertTrue(samlIdPSingleSignOnParticipationStrategy.supports(ssoRequest));
        assertFalse(samlIdPSingleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }
}
