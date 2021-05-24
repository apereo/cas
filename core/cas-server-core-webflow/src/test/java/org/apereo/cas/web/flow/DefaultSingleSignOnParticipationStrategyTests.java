package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.model.TriStateBoolean;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("Webflow")
public class DefaultSingleSignOnParticipationStrategyTests {
    @Test
    public void verifyParticipationDisabled() {
        val mgr = mock(ServicesManager.class);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val sso = new SingleSignOnProperties().setSsoEnabled(false);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyParticipatesForRenew() {
        val mgr = mock(ServicesManager.class);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(true).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.isParticipating(ssoRequest)
            || strategy.isCreateCookieOnRenewedAuthentication(ssoRequest) == TriStateBoolean.TRUE);
    }

    @Test
    public void verifyParticipatesForRenewDisabled() {
        val mgr = mock(ServicesManager.class);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyDoesNotParticipateForService() {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso()).thenReturn(false);
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), plan);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyCookieCreationByService() {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setCreateCookieOnRenewedAuthentication(TriStateBoolean.FALSE);
        when(registeredService.getSingleSignOnParticipationPolicy()).thenReturn(policy);
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso, mock(TicketRegistrySupport.class), plan);
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        val create = strategy.isCreateCookieOnRenewedAuthentication(ssoRequest);
        assertTrue(create.isFalse());
    }

    @Test
    public void verifyRegisteredServiceFromContextEvaluatedBeforeService() {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val callbackRegisteredService = CoreAuthenticationTestUtils.getRegisteredService("https://cas/idp/profile/SAML2/Callback");

        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso()).thenReturn(false);
        when(callbackRegisteredService.getAccessStrategy().isServiceAccessAllowedForSso()).thenReturn(true);

        when(mgr.findServiceBy(any(Service.class))).thenReturn(callbackRegisteredService);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));

    }

    @Test
    public void verifyRegisteredServiceWithValidSso() {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso()).thenReturn(true);
        when(registeredService.getSingleSignOnParticipationPolicy()).thenReturn(new DefaultRegisteredServiceSingleSignOnParticipationPolicy());
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        val sso = new SingleSignOnProperties();
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        when(ticketRegistrySupport.getTicketState(anyString())).thenReturn(tgt);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            ticketRegistrySupport, mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    public void verifyRegisteredServiceWithValidSsoAndServiceExpPolicy() {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso()).thenReturn(true);
        when(registeredService.getTicketGrantingTicketExpirationPolicy())
            .thenReturn(new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy(1));
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setCreated(ZonedDateTime.now(ZoneOffset.UTC).minusHours(1));
        val sso = new SingleSignOnProperties();
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        when(ticketRegistrySupport.getTicketState(anyString())).thenReturn(tgt);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            ticketRegistrySupport, mock(AuthenticationServiceSelectionPlan.class));
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(request)
            .requestContext(context)
            .build();
        assertFalse(strategy.isParticipating(ssoRequest));
    }
}
