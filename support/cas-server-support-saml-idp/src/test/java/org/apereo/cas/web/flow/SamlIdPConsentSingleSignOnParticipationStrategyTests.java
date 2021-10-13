package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasConsentCoreConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy;
import org.apereo.cas.support.saml.SamlIdPTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPConsentSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAML")
@Import(CasConsentCoreConfiguration.class)
public class SamlIdPConsentSingleSignOnParticipationStrategyTests extends BaseSamlIdPWebflowTests {

    @Autowired
    @Qualifier("singleSignOnParticipationStrategy")
    private SingleSignOnParticipationStrategy singleSignOnParticipationStrategy;

    @Test
    public void verifyIdPNeedsConsentOperation() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val principal = RegisteredServiceTestUtils.getPrincipal("casuser", CollectionUtils.wrap("uid", "CAS-System"));
        val authn = RegisteredServiceTestUtils.getAuthentication(principal);
        val cookie = new MockTicketGrantingTicket(authn);
        val issuer = UUID.randomUUID().toString();

        val registeredService = SamlIdPTestUtils.getSamlRegisteredService(issuer);
        registeredService.setAttributeReleasePolicy(new ReturnAllowedAttributeReleasePolicy(List.of("uid")));
        val service = RegisteredServiceTestUtils.getService(issuer);

        val authnRequest = getAuthnRequestFor(issuer);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build()
            .attribute(AuthnRequest.class.getName(), authnRequest)
            .attribute(Issuer.class.getName(), issuer)
            .attribute(Service.class.getName(), service)
            .attribute(RegisteredService.class.getName(), registeredService)
            .attribute(Authentication.class.getName(), authn)
            .attribute(TicketGrantingTicket.class.getName(), cookie);
        assertFalse(singleSignOnParticipationStrategy.isParticipating(ssoRequest));
    }
}
