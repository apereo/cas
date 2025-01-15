package org.apereo.cas.web.flow;

import org.apereo.cas.BaseDelegatedAuthenticationTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy;
import org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DelegatedAuthenticationSingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("Delegation")
class DelegatedAuthenticationSingleSignOnParticipationStrategyTests extends BaseDelegatedAuthenticationTests {
    private SingleSignOnParticipationStrategy getSingleSignOnStrategy(final RegisteredService registeredService) {

        val authenticationExecutionPlan = new DefaultAuthenticationEventExecutionPlan(new DefaultAuthenticationHandlerResolver(), tenantExtractor);
        authenticationExecutionPlan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());

        servicesManager.save(registeredService);
        return new DelegatedAuthenticationSingleSignOnParticipationStrategy(servicesManager,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            ticketRegistrySupport);
    }

    @Test
    void verifyNoServiceOrPolicy() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val svc = CoreAuthenticationTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val policy = new DefaultRegisteredServiceAccessStrategy();
        when(svc.getAccessStrategy()).thenReturn(policy);

        val strategy = getSingleSignOnStrategy(svc);
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();

        assertFalse(strategy.supports(ssoRequest));
        assertTrue(strategy.isParticipating(ssoRequest));

        WebUtils.putRegisteredService(context, svc);
        assertEquals(0, strategy.getOrder());
        assertTrue(strategy.supports(ssoRequest));
        assertTrue(strategy.isParticipating(ssoRequest));

        policy.setDelegatedAuthenticationPolicy(null);
        assertFalse(strategy.supports(ssoRequest));
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifySsoWithMismatchedClient() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAccessStrategy();
        policy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy().setAllowedProviders(List.of("Client2")));
        svc.setAccessStrategy(policy);

        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(svc.getServiceId()));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(
            Map.of(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, List.of("CAS")));

        val tgt = new MockTicketGrantingTicket(authentication);
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifySsoWithMissingClientAndExclusive() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAccessStrategy();
        policy.setDelegatedAuthenticationPolicy(
            new DefaultRegisteredServiceDelegatedAuthenticationPolicy()
                .setExclusive(true)
                .setAllowedProviders(List.of("CAS")));
        svc.setAccessStrategy(policy);

        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(svc.getServiceId()));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(Map.of());

        val tgt = new MockTicketGrantingTicket(authentication);
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertFalse(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyTgtIsExpired() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService(svc.getServiceId()));
        val authentication = CoreAuthenticationTestUtils.getAuthentication(Map.of());
        val tgt = new MockTicketGrantingTicket(authentication);
        tgt.markTicketExpired();
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertThrows(InvalidTicketException.class, () -> strategy.isParticipating(ssoRequest));
    }
}
