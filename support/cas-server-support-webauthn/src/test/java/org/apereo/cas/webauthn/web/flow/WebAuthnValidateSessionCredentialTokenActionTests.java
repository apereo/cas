package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnMultifactorAttestationTrustSourceFidoProperties;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.core.SessionManager;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnValidateSessionCredentialTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.web-authn.core.trust-source.fido.legal-header=" + WebAuthnMultifactorAttestationTrustSourceFidoProperties.DEFAULT_LEGAL_HEADER,
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org",
        "cas.authn.mfa.web-authn.core.allow-primary-authentication=true"
    })
class WebAuthnValidateSessionCredentialTokenActionTests {
    private static final String SAMPLE_TOKEN = "mO2ST2ZLIZCP6VmGDkiIX-_-VNXfOJQ6TjCwUFSCA3Y";

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_VALIDATE_SESSION_CREDENTIAL_TOKEN)
    private Action webAuthnValidateSessionCredentialTokenAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnSessionManager")
    private SessionManager webAuthnSessionManager;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyMissingToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);
        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    void verifyEmptySessionForToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("token", SAMPLE_TOKEN);
        WebUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);
        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    void verifyNoUserForToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val token = webAuthnSessionManager.createSession(ByteArray.fromBase64Url(SAMPLE_TOKEN));
        context.setParameter("token", token.getBase64Url());

        WebUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);

        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    void verifySuccessAuthForToken() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val userHandle = ByteArray.fromBase64Url(SAMPLE_TOKEN);
        val token = webAuthnSessionManager.createSession(userHandle);
        context.setParameter("token", token.getBase64Url());

        webAuthnCredentialRepository.addRegistrationByUsername("casuser",
            CredentialRegistration.builder()
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url("casuser"))
                    .userHandle(userHandle)
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .userIdentity(UserIdentity.builder()
                    .name("casuser")
                    .displayName("CAS")
                    .id(userHandle)
                    .build())
                .build());
        WebUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);

        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
    }

}
