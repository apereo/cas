package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.execution.Action;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorValidateTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleAuthenticatorValidateTokenActionTests.TestMultifactorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
})
class GoogleAuthenticatorValidateTokenActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_TOKEN)
    private Action action;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier("dummyProvider")
    private MultifactorAuthenticationProvider dummyProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySuccessfulValidation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val acct = GoogleAuthenticatorAccount
            .builder()
            .username(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of(666777))
            .build();
        googleAuthenticatorAccountRegistry.save(acct);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(acct.getUsername()), context);

        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, dummyProvider);
        assertThrows(IllegalArgumentException.class, () -> action.execute(context));

        context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "111222");
        context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, String.valueOf(acct.getId()));
        assertThrows(FailedLoginException.class, () -> action.execute(context));

        context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, acct.getScratchCodes().getFirst().toString());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }

    @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
    static class TestMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
