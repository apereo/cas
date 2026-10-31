package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.BaseWebAuthnCredentialRepositoryTests;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.core.RegistrationStorage;
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
 * This is {@link WebAuthnMultifactorAccountProfilePrepareActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
class WebAuthnMultifactorAccountProfilePrepareActionTests {

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_MFA_PREPARE)
    private Action webAuthnAccountProfilePrepareAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCOUNT_PROFILE_WEBAUTHN_RENAME_DEVICE)
    private Action webAuthnAccountProfileRenameDeviceAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private RegistrationStorage webAuthnCredentialRepository;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyRenameDevice() throws Exception {
        val username = UUID.randomUUID().toString();
        val registration = BaseWebAuthnCredentialRepositoryTests.getCredentialRegistration(username);
        webAuthnCredentialRepository.addRegistrationByUsername(username, registration);
        val credentialId = registration.getCredential().getCredentialId();

        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        context.setParameter("id", credentialId.getBase64Url());
        context.setParameter("name", "Work laptop");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, webAuthnAccountProfileRenameDeviceAction.execute(context).getId());

        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(username), context);
        context.setParameter("name", " ");
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, webAuthnAccountProfileRenameDeviceAction.execute(context).getId());

        context.setParameter("name", "Work laptop");
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, webAuthnAccountProfileRenameDeviceAction.execute(context).getId());
        val renamed = webAuthnCredentialRepository.getRegistrationByUsernameAndCredentialId(username, credentialId).orElseThrow();
        assertEquals("Work laptop", renamed.getCredentialNickname());
        assertEquals(1, webAuthnCredentialRepository.getRegistrationsByUsername(username).size());
    }

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString()), context);
        val eventId = webAuthnAccountProfilePrepareAction.execute(context);
        assertNull(eventId);
        assertNotNull(MultifactorAuthenticationWebflowUtils.getMultifactorAuthenticationProvider(context));
        assertTrue(context.getFlowScope().contains("webauthnAccountProfileRegistrationEnabled"));
    }

}
