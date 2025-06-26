package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.core.SessionManager;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.webflow.execution.Action;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnAccountSaveRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "CasFeatureModule.AccountManagement.enabled=true")
class WebAuthnAccountSaveRegistrationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_SAVE_ACCOUNT_REGISTRATION)
    private Action webAuthnSaveAccountRegistrationAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier(SessionManager.BEAN_NAME)
    private SessionManager webAuthnSessionManager;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    
    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setParameter("sessionToken", EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8)));
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);
        val request = context.getHttpServletRequest();
        request.setSession(new MockHttpSession());

        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authn, context);
        var result = webAuthnSaveAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(),
            CredentialRegistration.builder()
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                    .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());
        val token = EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8));
        val sessionId = webAuthnSessionManager.createSession(request, ByteArray.fromBase64(token));
        context.setParameter("sessionToken", sessionId.getBase64Url());

        result = webAuthnSaveAccountRegistrationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }
}
