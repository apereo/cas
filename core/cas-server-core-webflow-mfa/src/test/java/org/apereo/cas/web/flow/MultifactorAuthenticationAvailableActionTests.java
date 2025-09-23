package org.apereo.cas.web.flow;

import org.apereo.cas.BaseCasWebflowMultifactorAuthenticationTests;
import org.apereo.cas.authentication.DefaultMultifactorAuthenticationFailureModeEvaluator;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link MultifactorAuthenticationAvailableActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowMfaActions")
@Execution(ExecutionMode.SAME_THREAD)
class MultifactorAuthenticationAvailableActionTests {

    @Nested
    class DefaultTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_AVAILABLE)
        private Action mfaAvailableAction;

        @Test
        void verifyOperations() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService();
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

            val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, provider);

            val event = mfaAvailableAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }
    }

    @Nested
    @Import(FailureModeNoneTests.MultifactorProviderTestConfiguration.class)
    class FailureModeNoneTests extends BaseCasWebflowMultifactorAuthenticationTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_MFA_CHECK_AVAILABLE)
        private Action mfaAvailableAction;

        @Autowired
        @Qualifier("dummyProvider")
        private MultifactorAuthenticationProvider dummyProvider;

        @Test
        void verifyOperations() throws Throwable {
            val context = MockRequestContext.create(applicationContext);

            val service = RegisteredServiceTestUtils.getRegisteredService();
            servicesManager.save(service);
            WebUtils.putRegisteredService(context, service);

            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), context);

            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);

            val event = mfaAvailableAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_YES, event.getId());
        }

        @TestConfiguration(value = "MultifactorProviderTestConfiguration", proxyBeanMethods = false)
        static class MultifactorProviderTestConfiguration {
            @Bean
            public MultifactorAuthenticationProvider dummyProvider(final CasConfigurationProperties casProperties) {
                val provider = new TestMultifactorAuthenticationProvider();
                provider.setAvailable(false);
                provider.setFailureMode(MultifactorAuthenticationProviderFailureModes.NONE);
                provider.setFailureModeEvaluator(new DefaultMultifactorAuthenticationFailureModeEvaluator(casProperties));
                return provider;
            }
        }
    }
}
