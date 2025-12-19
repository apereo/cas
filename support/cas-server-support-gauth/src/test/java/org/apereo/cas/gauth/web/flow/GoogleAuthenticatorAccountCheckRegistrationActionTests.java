package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorAccountCheckRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
class GoogleAuthenticatorAccountCheckRegistrationActionTests {

    @SpringBootTest(classes = {
        GoogleAuthenticatorAccountCheckRegistrationActionTests.TestMultifactorTestConfiguration.class,
        BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
    })
    abstract static class BaseTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_CHECK_ACCOUNT_REGISTRATION)
        protected Action action;

        @Autowired
        protected ConfigurableApplicationContext applicationContext;

        @Autowired
        @Qualifier("dummyProvider")
        protected MultifactorAuthenticationProvider dummyProvider;
    }

    @Nested
    class DefaultTests extends BaseTests {
        @Test
        void verifyRegistration() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);
            val event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_REGISTER, event.getId());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.gauth.core.device-registration-enabled=false")
    class RegistrationDisabledTests extends BaseTests {
        @Test
        void verifyRegistration() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
            MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);
            val event = action.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_STOP, event.getId());
        }
    }

    @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
    static class TestMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
