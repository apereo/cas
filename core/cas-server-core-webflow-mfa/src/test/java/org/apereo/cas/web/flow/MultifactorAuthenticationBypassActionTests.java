package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.AlwaysAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationBypassActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@Execution(ExecutionMode.SAME_THREAD)
class MultifactorAuthenticationBypassActionTests {

    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProviderNeverBypass(final ConfigurableApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-never");
            provider.setBypassEvaluator(new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext));
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderUnavailable(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-unavailable");
            provider.setAvailable(false);
            provider.setBypassEvaluator(new NeverAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext));
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderAlwaysBypass(
            final ConfigurableApplicationContext applicationContext,
            final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-always");
            provider.setBypassEvaluator(new AlwaysAllowMultifactorAuthenticationProviderBypassEvaluator(applicationContext));
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }
    }

    @Nested
    @Import(MultifactorAuthenticationBypassActionTests.MultifactorAuthenticationTestConfiguration.class)
    class AlwaysBypassTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS)
        private Action mfaBypassAction;

        @Autowired
        @Qualifier("dummyProviderAlwaysBypass")
        private MultifactorAuthenticationProvider dummyProvider;


        @Test
        void verifyOperations() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);

            val transition = mock(Transition.class);
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_BYPASS);
            context.setCurrentTransition(transition);
            var event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
            
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            context.setCurrentTransition(transition);
            event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }
    }

    @Nested
    @Import(MultifactorAuthenticationBypassActionTests.MultifactorAuthenticationTestConfiguration.class)
    class DefaultTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS)
        private Action mfaBypassAction;

        @Autowired
        @Qualifier("dummyProviderNeverBypass")
        private MultifactorAuthenticationProvider dummyProvider;

        @Test
        void verifyOperations() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);

            val transition = mock(Transition.class);
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_BYPASS);
            context.setCurrentTransition(transition);
            var event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());

            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            context.setCurrentTransition(transition);
            event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_NO, event.getId());
        }
    }

    @Nested
    @Import(MultifactorAuthenticationBypassActionTests.MultifactorAuthenticationTestConfiguration.class)
    class FailureModeBypassTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS)
        private Action mfaBypassAction;

        @Autowired
        @Qualifier("dummyProviderUnavailable")
        private MultifactorAuthenticationProvider dummyProvider;

        @Test
        void verifyOperations() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setFailureMode(MultifactorAuthenticationProviderFailureModes.OPEN);
            service.setMultifactorAuthenticationPolicy(policy);
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);
            val transition = mock(Transition.class);
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            context.setCurrentTransition(transition);

            val event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }
    }
}
