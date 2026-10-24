package org.apereo.cas.web.flow.account;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.support.password.PasswordExpiringWarningMessageDescriptor;
import org.apereo.cas.config.CasCoreAuditAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.support.events.CasEventRepository;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.AbstractWebflowActionsTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link PrepareAccountProfileViewActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = {
    "CasFeatureModule.AccountManagement.enabled=true",
    "cas.view.authorized-services-on-successful-login=true"
})
@Import(PrepareAccountProfileViewActionTests.AuditTestConfiguration.class)
@ImportAutoConfiguration({
    CasCoreWebflowAutoConfiguration.class,
    CasCoreAuditAutoConfiguration.class
})
class PrepareAccountProfileViewActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE)
    private Action prepareAccountProfileViewAction;

    @Test
    void verifyOperation() throws Throwable {
        val registeredService1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        registeredService1.setEvaluationOrder(200);
        val registeredService2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        registeredService2.setEvaluationOrder(100);
        getServicesManager().save(registeredService1);
        getServicesManager().save(registeredService2);

        val context = MockRequestContext.create(applicationContext);
        val tgt = new TicketGrantingTicketImpl(RandomUtils.randomAlphabetic(8),
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        getTicketRegistry().addTicket(tgt);
        
        val result = prepareAccountProfileViewAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthorizedServices(context));
        assertNotNull(WebUtils.getSingleSignOnSessions(context));
        val list = WebUtils.getAuthorizedServices(context);
        assertFalse(list.isEmpty());
        assertTrue(list.indexOf(registeredService1) > list.indexOf(registeredService2));
        assertNotNull(WebUtils.getAuthentication(context));

        val session = (AccountSingleSignOnSession) WebUtils.getSingleSignOnSessions(context).getFirst();
        assertNotNull(session.getAuthenticationDate());
        assertNotNull(session.getPayload());
        assertNotNull(session.getPrincipal());
        assertNotNull(session.getClientIpAddress());
        assertNotNull(session.getUserAgent());
        assertTrue(context.getFlowScope().contains("auditLog"));

        val activity = context.getFlowScope().get("signInActivity", List.class);
        assertEquals(2, activity.size());
        val latest = (AccountSignInActivity) activity.getFirst();
        assertEquals("failure", latest.getOutcome());
        assertEquals("198.51.100.7", latest.getClientIpAddress());
        assertEquals("Lisbon, Portugal", latest.getLocation());
        assertEquals("success", ((AccountSignInActivity) activity.getLast()).getOutcome());
        assertFalse(context.getFlowScope().contains("passwordExpirationDays"));
    }

    @Test
    void verifyPasswordExpiration() throws Throwable {
        val authentication = DefaultAuthenticationBuilder.newInstance(CoreAuthenticationTestUtils.getAuthentication())
            .addWarning(new PasswordExpiringWarningMessageDescriptor("Password expires soon", 5))
            .build();
        val context = MockRequestContext.create(applicationContext);
        val tgt = new TicketGrantingTicketImpl(RandomUtils.randomAlphabetic(8), authentication, NeverExpiresExpirationPolicy.INSTANCE);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        getTicketRegistry().addTicket(tgt);
        prepareAccountProfileViewAction.execute(context);
        assertEquals(5L, context.getFlowScope().get("passwordExpirationDays"));
        assertEquals(authentication.getAuthenticationDate().plusDays(5), context.getFlowScope().get("passwordExpirationDate"));
    }

    @TestConfiguration(value = "AuditTestConfiguration", proxyBeanMethods = false)
    static class AuditTestConfiguration implements AuditTrailExecutionPlanConfigurer {
        @Override
        public void configureAuditTrailExecutionPlan(final AuditTrailExecutionPlan plan) {
            plan.registerAuditTrailManager(new MockAuditTrailManager());
        }

        @Bean(name = CasEventRepository.BEAN_NAME)
        public CasEventRepository casEventRepository() {
            val repository = mock(CasEventRepository.class);
            when(repository.getEventsForPrincipal(anyString(), any(ZonedDateTime.class))).thenAnswer(invocation -> Stream.of(
                new CasEvent()
                    .setType(CasTicketGrantingTicketCreatedEvent.class.getCanonicalName())
                    .setPrincipalId(invocation.getArgument(0))
                    .setCreationTime(Instant.now().minusSeconds(3600))
                    .putClientIpAddress("203.0.113.9")
                    .putAgent("Firefox"),
                new CasEvent()
                    .setType(CasAuthenticationTransactionFailureEvent.class.getCanonicalName())
                    .setPrincipalId(invocation.getArgument(0))
                    .setCreationTime(Instant.now())
                    .putClientIpAddress("198.51.100.7")
                    .putAgent("Safari")
                    .putGeoAddress("Lisbon, Portugal"),
                new CasEvent()
                    .setType("org.example.UnrelatedEvent")
                    .setPrincipalId(invocation.getArgument(0))
                    .setCreationTime(Instant.now())));
            return repository;
        }
    }
}
