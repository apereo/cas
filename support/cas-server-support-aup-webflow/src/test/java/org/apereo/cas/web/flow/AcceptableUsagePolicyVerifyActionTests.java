package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyVerifyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
public class AcceptableUsagePolicyVerifyActionTests {

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    public class DefaultTests extends BaseAcceptableUsagePolicyActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        private Action acceptableUsagePolicyVerifyAction;

        @Test
        public void verifyAction() throws Exception {
            val user = UUID.randomUUID().toString();
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }

        @Test
        public void verifyActionAccepted() throws Exception {
            val user = UUID.randomUUID().toString();
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            acceptableUsagePolicyRepository.submit(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }


        @Test
        public void verifyActionWithService() throws Exception {
            val user = UUID.randomUUID().toString();
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            val registeredService = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
            policy.setEnabled(false);
            registeredService.setAcceptableUsagePolicy(policy);
            WebUtils.putRegisteredService(context, registeredService);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @TestPropertySource(properties = "cas.acceptable-usage-policy.core.enabled=false")
    public class NoOpSkippedTests extends BaseAcceptableUsagePolicyActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        private Action acceptableUsagePolicyVerifyAction;

        @Test
        public void verifyAction() throws Exception {
            val user = UUID.randomUUID().toString();
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }

        @Test
        public void verifyNoOpRepository() throws Exception {
            val context = new MockRequestContext();
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            assertTrue(acceptableUsagePolicyRepository.fetchPolicy(context).isEmpty());
            assertFalse(acceptableUsagePolicyRepository.submit(context));
            assertTrue(acceptableUsagePolicyRepository.verify(context).getStatus().isUndefined());
        }
    }
}
