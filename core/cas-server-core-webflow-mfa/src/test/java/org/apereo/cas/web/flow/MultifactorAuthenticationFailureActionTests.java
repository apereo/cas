package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationFailureActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@Execution(ExecutionMode.SAME_THREAD)
@SuppressWarnings("EffectivelyPrivate")
class MultifactorAuthenticationFailureActionTests {

    @TestConfiguration(value = "MultifactorAuthenticationTestConfiguration", proxyBeanMethods = false)
    static class MultifactorAuthenticationTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProviderOpen(final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider();
            provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.OPEN);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderClosed(final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-closed");
            provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.CLOSED);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderPhantom(final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-phantom");
            provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.PHANTOM);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderNone(final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-none");
            provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.NONE);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }

        @Bean
        public MultifactorAuthenticationProvider dummyProviderUndefined(final CasConfigurationProperties casProperties) {
            val provider = new TestMultifactorAuthenticationProvider("mfa-undefined");
            provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.UNDEFINED);
            provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
            return provider;
        }
    }

    private static class BaseMultifactorActionTests extends BaseCasWebflowMultifactorAuthenticationTests {

        @Autowired
        @Qualifier("dummyProviderOpen")
        protected MultifactorAuthenticationProvider dummyProviderOpen;

        @Autowired
        @Qualifier("dummyProviderClosed")
        protected MultifactorAuthenticationProvider dummyProviderClosed;

        @Autowired
        @Qualifier("dummyProviderNone")
        protected MultifactorAuthenticationProvider dummyProviderNone;

        @Autowired
        @Qualifier("dummyProviderUndefined")
        protected MultifactorAuthenticationProvider dummyProviderUndefined;

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_FAILURE)
        protected Action mfaFailureAction;

        protected void executeAction(final MultifactorAuthenticationProvider provider,
                                     final MultifactorAuthenticationProviderFailureModes serviceMode,
                                     final String transitionId) throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val service = RegisteredServiceTestUtils.getRegisteredService(UUID.randomUUID().toString());

            if (serviceMode != null) {
                val policy = new DefaultRegisteredServiceMultifactorPolicy();
                policy.setFailureMode(serviceMode);
                service.setMultifactorAuthenticationPolicy(policy);
            } else {
                service.setMultifactorAuthenticationPolicy(null);
            }
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);
            val event = mfaFailureAction.execute(context);
            assertEquals(transitionId, event.getId());
        }
    }

    @Nested
    @Import(MultifactorAuthenticationTestConfiguration.class)
    class UnavailableModes extends BaseMultifactorActionTests {
        @Test
        void verifyOperations() throws Throwable {
            executeAction(dummyProviderClosed, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
            executeAction(dummyProviderNone, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
            executeAction(dummyProviderUndefined, null, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
        }
    }

    @Nested
    @Import(MultifactorAuthenticationTestConfiguration.class)
    class OpenMode extends BaseMultifactorActionTests {
        @Test
        void verifyOperations() throws Throwable {
            executeAction(dummyProviderOpen, null, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
    }

    @Nested
    @Import(MultifactorAuthenticationTestConfiguration.class)
    class PhantomMode extends BaseMultifactorActionTests {
        @Test
        void verifyOperations() throws Throwable {
            executeAction(dummyProviderClosed, MultifactorAuthenticationProviderFailureModes.PHANTOM, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }
    }

}
