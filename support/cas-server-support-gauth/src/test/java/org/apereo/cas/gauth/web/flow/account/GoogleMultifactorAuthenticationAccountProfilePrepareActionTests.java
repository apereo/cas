package org.apereo.cas.gauth.web.flow.account;

import module java.base;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.gauth.BaseGoogleAuthenticatorTests;
import org.apereo.cas.gauth.credential.BaseGoogleAuthenticatorTokenCredentialRepository;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link GoogleMultifactorAuthenticationAccountProfilePrepareActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseGoogleAuthenticatorTests.SharedTestConfiguration.class,
    CasCoreWebflowAutoConfiguration.class
},
    properties = "CasFeatureModule.AccountManagement.enabled=true")
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
class GoogleMultifactorAuthenticationAccountProfilePrepareActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_PREPARE)
    private Action googleAccountProfilePrepareAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_GOOGLE_MFA_SCRATCH_CODES)
    private Action googleAccountProfileScratchCodesAction;

    @Autowired
    @Qualifier(BaseGoogleAuthenticatorTokenCredentialRepository.BEAN_NAME)
    private OneTimeTokenCredentialRepository googleAuthenticatorAccountRegistry;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRegenerateScratchCodes() throws Exception {
        val username = UUID.randomUUID().toString();
        val account = googleAuthenticatorAccountRegistry.save(googleAuthenticatorAccountRegistry.create(username));
        val previousCodes = googleAuthenticatorAccountRegistry.get(username, account.getId()).getScratchCodes()
            .stream().map(Number::longValue).toList();

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        context.setParameter("id", String.valueOf(account.getId()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, googleAccountProfileScratchCodesAction.execute(context).getId());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(username), context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, googleAccountProfileScratchCodesAction.execute(context).getId());
        val shown = context.getFlashScope().get(GoogleMultifactorAuthenticationAccountProfileScratchCodesAction.FLASH_SCOPE_ATTR_SCRATCH_CODES, List.class);
        assertNotNull(shown);
        assertFalse(shown.isEmpty());
        val storedCodes = googleAuthenticatorAccountRegistry.get(username, account.getId()).getScratchCodes()
            .stream().map(Number::longValue).toList();
        assertNotEquals(previousCodes, storedCodes);
        assertEquals(shown, storedCodes.stream().map(String::valueOf).toList());
    }

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        val eventId = googleAccountProfilePrepareAction.execute(context);
        assertNull(eventId);
        assertNotNull(MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(context));
        assertTrue(context.getFlowScope().contains("gauthAccountProfileRegistrationEnabled"));
    }

    @Test
    void verifyRegistrationDisabled() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        MultifactorAuthenticationWebflowUtils.putMultifactorDeviceRegistrationEnabled(context, false);
        googleAccountProfilePrepareAction.execute(context);
        assertNotNull(MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(context));
        assertFalse(context.getFlowScope().getBoolean("gauthAccountProfileRegistrationEnabled"));
    }
}
