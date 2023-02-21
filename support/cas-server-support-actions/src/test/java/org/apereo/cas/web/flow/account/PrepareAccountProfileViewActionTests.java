package org.apereo.cas.web.flow.account;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.audit.AuditTrailExecutionPlanConfigurer;
import org.apereo.cas.audit.spi.MockAuditTrailManager;
import org.apereo.cas.audit.spi.config.CasCoreAuditConfiguration;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.AbstractWebflowActionsTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.config.CasWebflowAccountProfileConfiguration;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link PrepareAccountProfileViewActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = {
    "CasFeatureModule.AccountManagement.enabled=true",
    "cas.view.authorized-services-on-successful-login=true"
})
@Import({
    PrepareAccountProfileViewActionTests.AuditTestConfiguration.class,
    CasWebflowAccountProfileConfiguration.class,
    CasCoreAuditConfiguration.class
})
public class PrepareAccountProfileViewActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PREPARE_ACCOUNT_PROFILE)
    private Action prepareAccountProfileViewAction;

    @Test
    public void verifyOperation() throws Exception {
        val registeredService1 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        registeredService1.setEvaluationOrder(200);
        val registeredService2 = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString(), Map.of());
        registeredService2.setEvaluationOrder(100);
        getServicesManager().save(registeredService1);
        getServicesManager().save(registeredService2);

        val context = new MockRequestContext();
        val tgt = new TicketGrantingTicketImpl(RandomUtils.randomAlphabetic(8),
            CoreAuthenticationTestUtils.getAuthentication(), NeverExpiresExpirationPolicy.INSTANCE);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        getTicketRegistry().addTicket(tgt);

        context.setExternalContext(new MockExternalContext());
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val result = prepareAccountProfileViewAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
        assertNotNull(WebUtils.getAuthorizedServices(context));
        assertNotNull(WebUtils.getSingleSignOnSessions(context));
        val list = WebUtils.getAuthorizedServices(context);
        assertFalse(list.isEmpty());
        assertTrue(list.indexOf(registeredService1) > list.indexOf(registeredService2));
        assertNotNull(WebUtils.getAuthentication(context));

        val session = (PrepareAccountProfileViewAction.SingleSignOnSession) WebUtils.getSingleSignOnSessions(context).get(0);
        assertNotNull(session.getAuthenticationDate());
        assertNotNull(session.getPayload());
        assertNotNull(session.getPrincipal());
        assertNotNull(session.getClientIpAddress());
        assertNotNull(session.getUserAgent());
        assertTrue(context.getFlowScope().contains("auditLog"));
    }

    @TestConfiguration(value = "AuditTestConfiguration", proxyBeanMethods = false)
    public static class AuditTestConfiguration implements AuditTrailExecutionPlanConfigurer {
        @Override
        public void configureAuditTrailExecutionPlan(final AuditTrailExecutionPlan plan) {
            plan.registerAuditTrailManager(new MockAuditTrailManager());
        }
    }
}
