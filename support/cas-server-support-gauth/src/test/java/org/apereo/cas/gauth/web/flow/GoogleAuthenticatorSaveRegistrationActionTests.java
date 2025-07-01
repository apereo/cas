package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountCreateRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * This is {@link GoogleAuthenticatorSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@SpringBootTest(classes = {
    GoogleAuthenticatorSaveRegistrationActionTests.GoogleAuthenticatorSaveRegistrationActionTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
})
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "googleAuthenticatorAccountRegistry", mode = ResourceAccessMode.READ_WRITE)
class GoogleAuthenticatorSaveRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION)
    private Action googleSaveAccountRegistrationAction;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.gauth.core.multiple-device-registration-enabled=false")
    class MultipleRegistrationTests {
        @Test
        void verifyMultipleRegDisabled() throws Exception {
            val context = MockRequestContext.create(applicationContext);
            val acct = GoogleAuthenticatorAccount.builder()
                .username(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .secretKey("secret")
                .validationCode(123456)
                .scratchCodes(List.of())
                .id(RandomUtils.nextLong())
                .build();
            googleAuthenticatorAccountRegistry.save(acct);
            context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
        }
    }

    @Nested
    @TestPropertySource(properties = "cas.authn.mfa.gauth.core.multiple-device-registration-enabled=true")
    class DefaultTests {
        @Test
        void verifyAccountValidationFails() throws Throwable {
            val acct = GoogleAuthenticatorAccount.builder()
                .username(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .secretKey("secret")
                .validationCode(123456)
                .scratchCodes(List.of())
                .id(RandomUtils.nextLong())
                .build();

            val context = MockRequestContext.create(applicationContext);
            context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "918273");
            context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, acct.getName());
            context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
        }

        @Test
        void verifyAccountValidationOnly() throws Throwable {
            val acct = GoogleAuthenticatorAccount.builder()
                .username(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .secretKey("secret")
                .validationCode(123456)
                .scratchCodes(List.of())
                .id(RandomUtils.nextLong())
                .build();

            var context = MockRequestContext.create(applicationContext);
            context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, String.valueOf(acct.getValidationCode()));
            context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, acct.getName());
            context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "true");
            context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, acct);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, googleSaveAccountRegistrationAction.execute(context).getId());

            context = MockRequestContext.create();
            context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "987654");
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
            assertEquals(HttpStatus.UNAUTHORIZED.value(), context.getHttpServletResponse().getStatus());

            context = MockRequestContext.create(applicationContext);
            context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "112233");
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(context).getId());
            assertEquals(HttpStatus.UNAUTHORIZED.value(), context.getHttpServletResponse().getStatus());
        }
    }

    @TestConfiguration(value = "GoogleAuthenticatorSaveRegistrationActionTests", proxyBeanMethods = false)
    static class GoogleAuthenticatorSaveRegistrationActionTestConfiguration {
        @Bean
        public CasGoogleAuthenticator googleAuthenticatorInstance() {
            val auth = mock(CasGoogleAuthenticator.class);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(123456))).thenReturn(Boolean.TRUE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(987654))).thenReturn(Boolean.FALSE);
            when(auth.authorize(anyString(), ArgumentMatchers.eq(112233))).thenThrow(new IllegalArgumentException());
            return auth;
        }
    }
}
