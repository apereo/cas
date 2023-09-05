package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.mfa.TestMultifactorAuthenticationProvider;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.webflow.execution.Action;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorPrepareLoginActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = {
    GoogleAuthenticatorPrepareLoginActionTests.TestMultifactorTestConfiguration.class,
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class
},
    properties = "cas.authn.mfa.gauth.core.multiple-device-registration-enabled=true")
class GoogleAuthenticatorPrepareLoginActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_GOOGLE_PREPARE_LOGIN)
    private Action action;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    @Qualifier("dummyProvider")
    private MultifactorAuthenticationProvider dummyProvider;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create();
        val acct = GoogleAuthenticatorAccount
            .builder()
            .username("casuser")
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        googleAuthenticatorAccountRegistry.save(acct);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(acct.getUsername()), context);

        WebUtils.putMultifactorAuthenticationProvider(context, dummyProvider);
        assertNull(action.execute(context));
        assertTrue(WebUtils.isGoogleAuthenticatorMultipleDeviceRegistrationEnabled(context));
        assertNotNull(WebUtils.getOneTimeTokenAccounts(context));
    }

    @TestConfiguration(value = "TestMultifactorTestConfiguration", proxyBeanMethods = false)
    static class TestMultifactorTestConfiguration {
        @Bean
        public MultifactorAuthenticationProvider dummyProvider() {
            return new TestMultifactorAuthenticationProvider();
        }
    }
}
