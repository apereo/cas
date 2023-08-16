package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationPrincipalResolver;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.bypass.MultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.bypass.NeverAllowMultifactorAuthenticationProviderBypassEvaluator;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MultifactorAuthenticationBypassActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
class MultifactorAuthenticationBypassActionTests {

    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import(MultifactorAuthenticationBypassActionTests.MultifactorAuthenticationTestConfiguration.class)
    class DefaultTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS)
        private Action mfaBypassAction;

        @Autowired
        private ConfigurableApplicationContext configurableApplicationContext;

        @Test
        void verifyOperations() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val service = RegisteredServiceTestUtils.getRegisteredService();
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            configurableApplicationContext.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class)
                .forEach((key, value) -> ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, value, key));

            provider.setBypassEvaluator(NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance());
            WebUtils.putMultifactorAuthenticationProvider(context, provider);

            val transition = mock(Transition.class);
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_BYPASS);
            context.setCurrentTransition(transition);
            var event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());

            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            context.setCurrentTransition(transition);
            event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_NO, event.getId());

            val eval = mock(MultifactorAuthenticationProviderBypassEvaluator.class);
            when(eval.shouldMultifactorAuthenticationProviderExecute(any(), any(), any(), any(), any())).thenReturn(Boolean.FALSE);
            provider.setBypassEvaluator(eval);
            event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @Import(MultifactorAuthenticationBypassActionTests.MultifactorAuthenticationTestConfiguration.class)
    class FailureModeBypassTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_BYPASS)
        private Action mfaBypassAction;

        @Autowired
        private ConfigurableApplicationContext configurableApplicationContext;

        @Test
        void verifyOperations() throws Throwable {
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

            val service = RegisteredServiceTestUtils.getRegisteredService();
            val policy = new DefaultRegisteredServiceMultifactorPolicy();
            policy.setFailureMode(MultifactorAuthenticationProviderFailureModes.OPEN);
            service.setMultifactorAuthenticationPolicy(policy);
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            provider.setAvailable(false);
            configurableApplicationContext.getBeansOfType(MultifactorAuthenticationPrincipalResolver.class)
                .forEach((key, value) -> ApplicationContextProvider.registerBeanIntoApplicationContext(applicationContext, value, key));
            provider.setBypassEvaluator(NeverAllowMultifactorAuthenticationProviderBypassEvaluator.getInstance());
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            WebUtils.putMultifactorAuthenticationProvider(context, provider);

            val transition = mock(Transition.class);
            when(transition.getId()).thenReturn(CasWebflowConstants.TRANSITION_ID_SUCCESS);
            context.setCurrentTransition(transition);
            val event = mfaBypassAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }
    }
}
