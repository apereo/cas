package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.DefaultAuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.handler.DefaultAuthenticationHandlerResolver;
import org.apereo.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.services.AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.DefaultRegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Webflow")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebflowConfigurerTests.SharedTestConfiguration.class)
class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategyTests {

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    @Autowired
    @Qualifier(ServicesManager.BEAN_NAME)
    protected ServicesManager servicesManager;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    protected TicketRegistry ticketRegistry;

    @Autowired
    @Qualifier(TicketRegistrySupport.BEAN_NAME)
    protected TicketRegistrySupport ticketRegistrySupport;
    
    @Autowired
    @Qualifier(TenantExtractor.BEAN_NAME)
    protected TenantExtractor tenantExtractor;

    private SingleSignOnParticipationStrategy getSingleSignOnStrategy(final RegisteredService registeredService) {
        val authenticationExecutionPlan = new DefaultAuthenticationEventExecutionPlan(new DefaultAuthenticationHandlerResolver(), tenantExtractor);
        authenticationExecutionPlan.registerAuthenticationHandler(new SimpleTestUsernamePasswordAuthenticationHandler());

        servicesManager.save(registeredService);
        return new RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(servicesManager,
            ticketRegistrySupport,
            new DefaultAuthenticationServiceSelectionPlan(new DefaultAuthenticationServiceSelectionStrategy()),
            authenticationExecutionPlan, applicationContext);
    }

    @Test
    void verifyNoServiceOrPolicy() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(null);
        svc.setAuthenticationPolicy(policy);
        val strategy = getSingleSignOnStrategy(svc);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertTrue(strategy.isParticipating(ssoRequest));

        WebUtils.putRegisteredService(context, svc);
        assertEquals(0, strategy.getOrder());
        assertFalse(strategy.supports(ssoRequest));
        svc.setAuthenticationPolicy(null);
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifyNoServiceOrSso() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
        val strategy = getSingleSignOnStrategy(svc);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertFalse(strategy.supports(ssoRequest));
        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService("unknown"));
        assertFalse(strategy.supports(ssoRequest));
    }

    @Test
    void verifySsoWithMismatchedHandlers() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of("SomeOtherHandler"));
        policy.setCriteria(new AllowedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        svc.setAuthenticationPolicy(policy);

        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(svc.getServiceId()));
        val tgt = new MockTicketGrantingTicket("casuser");
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
    void verifySsoWithHandlers() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setRequiredAuthenticationHandlers(Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getSimpleName()));
        policy.setCriteria(new AnyAuthenticationHandlerRegisteredServiceAuthenticationPolicyCriteria());
        svc.setAuthenticationPolicy(policy);

        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(svc.getServiceId()));
        val tgt = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(tgt);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .httpServletRequest(context.getHttpServletRequest())
            .httpServletResponse(context.getHttpServletResponse())
            .requestContext(context)
            .build();
        assertTrue(strategy.supports(ssoRequest));
        assertTrue(strategy.isParticipating(ssoRequest));
    }

    @Test
    void verifySsoWithExcludedHandlers() throws Throwable {
        val context = MockRequestContext.create(applicationContext).setClientInfo();

        val svc = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        val policy = new DefaultRegisteredServiceAuthenticationPolicy();
        policy.setCriteria(new ExcludedAuthenticationHandlersRegisteredServiceAuthenticationPolicyCriteria());
        policy.setExcludedAuthenticationHandlers(
            Set.of(SimpleTestUsernamePasswordAuthenticationHandler.class.getName()));
        svc.setAuthenticationPolicy(policy);

        val strategy = getSingleSignOnStrategy(svc);

        WebUtils.putServiceIntoFlowScope(context, CoreAuthenticationTestUtils.getWebApplicationService(svc.getServiceId()));
        val tgt = new MockTicketGrantingTicket("casuser");
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
}
