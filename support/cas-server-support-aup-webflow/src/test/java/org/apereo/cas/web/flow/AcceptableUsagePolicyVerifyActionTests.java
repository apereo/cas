package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.aup.AcceptableUsagePolicyRepository;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.BaseWebBasedRegisteredService;
import org.apereo.cas.services.DefaultRegisteredServiceAcceptableUsagePolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyVerifyActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowAccountActions")
class AcceptableUsagePolicyVerifyActionTests {

    @TestConfiguration(value = "AcceptableUsagePolicyTestConfiguration", proxyBeanMethods = false)
    @EnableConfigurationProperties(CasConfigurationProperties.class)
    static class AcceptableUsagePolicyTestConfiguration {

        @Bean
        @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
        public AcceptableUsagePolicyRepository acceptableUsagePolicyRepository() {
            return AcceptableUsagePolicyRepository.noOp();
        }
    }

    @Nested
    @Import(AcceptableUsagePolicyTestConfiguration.class)
    class VerificationSkippedTests extends BaseAcceptableUsagePolicyActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        private Action acceptableUsagePolicyVerifyAction;

        @Test
        void verifyAction() throws Throwable {
            val user = UUID.randomUUID().toString();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }
    }

    @Nested
    class DefaultTests extends BaseAcceptableUsagePolicyActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        private Action acceptableUsagePolicyVerifyAction;

        @Test
        void verifyAction() throws Throwable {
            val user = UUID.randomUUID().toString();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }

        @Test
        void verifyActionAccepted() throws Throwable {
            val user = UUID.randomUUID().toString();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            acceptableUsagePolicyRepository.submit(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }

        @Test
        void verifyActionWithService() throws Throwable {
            val user = UUID.randomUUID().toString();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            val registeredService = (BaseWebBasedRegisteredService) RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            val policy = new DefaultRegisteredServiceAcceptableUsagePolicy();
            policy.setEnabled(false);
            registeredService.setAcceptableUsagePolicy(policy);
            WebUtils.putRegisteredService(context, registeredService);
            assertEquals(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, acceptableUsagePolicyVerifyAction.execute(context).getId());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.acceptable-usage-policy.core.enabled=false")
    class NoOpSkippedTests extends BaseAcceptableUsagePolicyActionTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_AUP_VERIFY)
        private Action acceptableUsagePolicyVerifyAction;

        @Test
        void verifyAction() throws Throwable {
            val user = UUID.randomUUID().toString();
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
            WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket(user));
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            assertNull(acceptableUsagePolicyVerifyAction.execute(context));
        }

        @Test
        void verifyNoOpRepository() throws Throwable {
            val context = new MockRequestContext();
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            assertTrue(acceptableUsagePolicyRepository.fetchPolicy(context).isEmpty());
            assertFalse(acceptableUsagePolicyRepository.submit(context));
            assertTrue(acceptableUsagePolicyRepository.verify(context).getStatus().isUndefined());
        }
    }
}
