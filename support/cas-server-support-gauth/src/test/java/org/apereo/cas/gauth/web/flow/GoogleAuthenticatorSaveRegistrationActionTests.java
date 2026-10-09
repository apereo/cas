package org.apereo.cas.gauth.web.flow;

import module java.base;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.repository.token.OneTimeTokenRepository;
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
@ResourceLock(value = "googleAuthenticatorAccountRegistry", mode = ResourceAccessMode.READ_WRITE)
class GoogleAuthenticatorSaveRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION)
    private Action googleSaveAccountRegistrationAction;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier(OneTimeTokenRepository.BEAN_NAME)
    private OneTimeTokenRepository oneTimeTokenRepository;

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
        void verifyScratchCodeRegistration() throws Exception {
            verifyRegistration(834251, List.of(834251, 223856));
        }

        @Test
        void verifyTotpRegistration() throws Exception {
            verifyRegistration(123456, List.of(834251, 223856));
        }

        private void verifyRegistration(final int token, final List<Number> scratchCodes) throws Exception {
            val account = GoogleAuthenticatorAccount.builder()
                .username(UUID.randomUUID().toString())
                .name(UUID.randomUUID().toString())
                .secretKey("secret")
                .scratchCodes(scratchCodes)
                .build();

            val validationContext = registrationContext(account, token);
            validationContext.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "true");
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, googleSaveAccountRegistrationAction.execute(validationContext).getId());
            assertEquals(0, googleAuthenticatorAccountRegistry.count(account.getUsername()));
            assertFalse(oneTimeTokenRepository.exists(account.getUsername(), token));
            assertEquals(scratchCodes, account.getScratchCodes());

            val submissionContext = registrationContext(account, token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, googleSaveAccountRegistrationAction.execute(submissionContext).getId());
            assertEquals(1, googleAuthenticatorAccountRegistry.count(account.getUsername()));
            val storedAccount = googleAuthenticatorAccountRegistry.get(account.getUsername()).iterator().next();
            assertTrue(storedAccount.getId() > 0);
            assertEquals(scratchCodes.stream().filter(code -> code.intValue() != token).toList(), storedAccount.getScratchCodes());
            assertTrue(oneTimeTokenRepository.exists(account.getUsername(), token));

            val replayContext = registrationContext(account, token);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleSaveAccountRegistrationAction.execute(replayContext).getId());
            assertEquals(HttpStatus.UNAUTHORIZED.value(), replayContext.getHttpServletResponse().getStatus());
            assertEquals(1, googleAuthenticatorAccountRegistry.count(account.getUsername()));
        }

        private MockRequestContext registrationContext(final GoogleAuthenticatorAccount account, final int token) throws Exception {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, String.valueOf(token));
            context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_ACCOUNT_NAME, account.getName());
            context.getFlowScope().put(OneTimeTokenAccountCreateRegistrationAction.FLOW_SCOPE_ATTR_ACCOUNT, account);
            return context;
        }

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

            context = MockRequestContext.create(applicationContext);
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
