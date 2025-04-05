package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.config.CasCoreMultitenancyAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.sso.SingleSignOnProperties;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceSingleSignOnParticipationPolicy;
import org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasCoreMultitenancyAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class
})
@EnableConfigurationProperties({CasConfigurationProperties.class, WebProperties.class})
@ExtendWith(CasTestExtension.class)
class DefaultSingleSignOnParticipationStrategyTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyParticipationDisabledWithService() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(true);
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        val sso = new SingleSignOnProperties().setSsoEnabled(false);
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso, mock(TicketRegistrySupport.class), plan);
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(registeredService.getServiceId()));

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyParticipationDisabled() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val context = MockRequestContext.create(applicationContext);

        val sso = new SingleSignOnProperties().setSsoEnabled(false);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyParticipatesForRenew() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val context = MockRequestContext.create(applicationContext);

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(true).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertTrue(strategy.isParticipating(ssoRequest)
                   || strategy.isCreateCookieOnRenewedAuthentication(ssoRequest) == TriStateBoolean.TRUE);
    }

    @Test
    void verifyParticipatesForRenewDisabled() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val context = MockRequestContext.create(applicationContext);

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        context.setParameter(CasProtocolConstants.PARAMETER_RENEW, "true");
        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyParticipateForServiceTgtExpirationPolicyWithoutTgt() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = RegisteredServiceTestUtils.getRegisteredService();
        registeredService.setTicketGrantingTicketExpirationPolicy(
            new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy(2));
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(registeredService.getServiceId()));
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso, mock(TicketRegistrySupport.class), plan);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyDoesNotParticipateForService() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(false);
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), plan);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyCookieCreationByService() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val policy = new DefaultRegisteredServiceSingleSignOnParticipationPolicy();
        policy.setCreateCookieOnRenewedAuthentication(TriStateBoolean.FALSE);
        when(registeredService.getSingleSignOnParticipationPolicy()).thenReturn(policy);
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        val plan = new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy());
        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso, mock(TicketRegistrySupport.class), plan);
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        val create = strategy.isCreateCookieOnRenewedAuthentication(ssoRequest);
        assertTrue(create.isFalse());
    }

    @Test
    void verifyRegisteredServiceFromContextEvaluatedBeforeService() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        val callbackRegisteredService = CoreAuthenticationTestUtils.getRegisteredService("https://cas/idp/profile/SAML2/Callback");

        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(false);
        when(callbackRegisteredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(true);

        when(mgr.findServiceBy(any(Service.class))).thenReturn(callbackRegisteredService);

        val context = MockRequestContext.create(applicationContext);

        val sso = new SingleSignOnProperties().setCreateSsoCookieOnRenewAuthn(false).setRenewAuthnEnabled(true);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            mock(TicketRegistrySupport.class), mock(AuthenticationServiceSelectionPlan.class));
        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));

    }

    @Test
    void verifyRegisteredServiceWithValidSso() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(true);
        when(registeredService.getSingleSignOnParticipationPolicy()).thenReturn(new DefaultRegisteredServiceSingleSignOnParticipationPolicy());
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        val tgt = new MockTicketGrantingTicket("casuser");
        val sso = new SingleSignOnProperties();
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        when(ticketRegistrySupport.getTicket(anyString())).thenReturn(tgt);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            ticketRegistrySupport, mock(AuthenticationServiceSelectionPlan.class));

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyRegisteredServiceWithValidSsoAndServiceExpPolicy() throws Throwable {
        val mgr = mock(ServicesManager.class);
        val registeredService = CoreAuthenticationTestUtils.getRegisteredService();
        when(registeredService.getAccessStrategy().isServiceAccessAllowedForSso(registeredService)).thenReturn(true);
        when(registeredService.getTicketGrantingTicketExpirationPolicy())
            .thenReturn(new DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy(1));
        when(mgr.findServiceBy(any(Service.class))).thenReturn(registeredService);

        val context = MockRequestContext.create(applicationContext);

        val tgt = new MockTicketGrantingTicket("casuser");
        tgt.setCreated(ZonedDateTime.now(ZoneOffset.UTC).minusHours(1));
        val sso = new SingleSignOnProperties();
        val ticketRegistrySupport = mock(TicketRegistrySupport.class);
        when(ticketRegistrySupport.getTicket(anyString())).thenReturn(tgt);
        val strategy = new DefaultSingleSignOnParticipationStrategy(mgr, sso,
            ticketRegistrySupport, mock(AuthenticationServiceSelectionPlan.class));

        WebUtils.putRegisteredService(context, registeredService);
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService());
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication("casuser"), context);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = getSingleSignOnParticipationRequest(context);
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    private static SingleSignOnParticipationRequest getSingleSignOnParticipationRequest(final MockRequestContext context) {
        return SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
    }
}
