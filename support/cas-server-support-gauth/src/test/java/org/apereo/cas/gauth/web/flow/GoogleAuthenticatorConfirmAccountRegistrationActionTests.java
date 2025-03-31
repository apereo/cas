package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountSaveRegistrationAction;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import javax.security.auth.login.FailedLoginException;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorConfirmAccountRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = {
    GoogleAuthenticatorPrepareLoginActionTests.TestMultifactorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
})
class GoogleAuthenticatorConfirmAccountRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_CONFIRM_REGISTRATION)
    private Action action;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRegisterWithoutVerify() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        var acct = GoogleAuthenticatorAccount
            .builder()
            .username(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of(187345))
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, String.valueOf(acct.getId()));
        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "false");
        context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "187345");
        assertThrows(FailedLoginException.class, () -> action.execute(context));
    }

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        var acct = GoogleAuthenticatorAccount
            .builder()
            .username(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .secretKey(UUID.randomUUID().toString())
            .validationCode(123456)
            .scratchCodes(List.of(987345))
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(acct.getUsername()), context);

        context.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, String.valueOf(acct.getId()));
        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "true");
        context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "112233");
        assertThrows(FailedLoginException.class, () -> action.execute(context));

        context.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "987345");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());

        acct = (GoogleAuthenticatorAccount) googleAuthenticatorAccountRegistry.get(acct.getId());
        assertEquals(GoogleAuthenticatorConfirmAccountRegistrationAction.ACCOUNT_PROPERTY_REGISTRATION_VERIFIED, acct.getProperties().getFirst());

        context.setParameter(OneTimeTokenAccountSaveRegistrationAction.REQUEST_PARAMETER_VALIDATE, "false");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
        assertTrue(acct.getProperties().isEmpty());
    }
}
