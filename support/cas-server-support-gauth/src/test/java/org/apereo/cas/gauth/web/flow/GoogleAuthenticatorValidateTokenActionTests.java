package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;
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
@SpringBootTest(classes = BaseGoogleAuthenticatorTests.SharedTestConfiguration.class)
class GoogleAuthenticatorValidateTokenActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_VALIDATE_TOKEN)
    private Action action;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifySuccessfulValidation() throws Throwable {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val acct = GoogleAuthenticatorAccount
            .builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of(666777))
            .build();
        googleAuthenticatorAccountRegistry.save(acct);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(acct.getUsername()), context);

        val provider = TestMultifactorAuthenticationProvider.registerProviderIntoApplicationContext(applicationContext);
        WebUtils.putMultifactorAuthenticationProvider(context, provider);

        assertThrows(IllegalArgumentException.class, () -> action.execute(context));
        
        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, "111222");
        request.setParameter(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, String.valueOf(acct.getId()));
        assertThrows(FailedLoginException.class, () -> action.execute(context));

        request.setParameter(GoogleAuthenticatorSaveRegistrationAction.REQUEST_PARAMETER_TOKEN, acct.getScratchCodes().get(0).toString());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
    }
}
