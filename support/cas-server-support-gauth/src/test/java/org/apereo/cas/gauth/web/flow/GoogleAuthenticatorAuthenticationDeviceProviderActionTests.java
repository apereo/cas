package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.GoogleAuthenticatorAccount;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.MultifactorAuthenticationDeviceProviderAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleAuthenticatorAuthenticationDeviceProviderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SpringBootTest(classes = {
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
},
    properties = "CasFeatureModule.AccountManagement.enabled=true")
@Tag("WebflowMfaActions")
class GoogleAuthenticatorAuthenticationDeviceProviderActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_DEVICE_PROVIDER)
    private MultifactorAuthenticationDeviceProviderAction googleAccountDeviceProviderAction;

    @Autowired
    @Qualifier("googleAuthenticatorAccountRegistry")
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Test
    void verifyOperation() throws Throwable {
        val acct = GoogleAuthenticatorAccount.builder()
            .username(UUID.randomUUID().toString())
            .name(UUID.randomUUID().toString())
            .secretKey("secret")
            .validationCode(123456)
            .scratchCodes(List.of())
            .build();
        googleAuthenticatorAccountRegistry.save(acct);

        val context = MockRequestContext.create();

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(acct.getUsername()), context);
        assertNull(googleAccountDeviceProviderAction.execute(context));
        assertEquals(1, WebUtils.getMultifactorAuthenticationRegisteredDevices(context).size());
    }

}
