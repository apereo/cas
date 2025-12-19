package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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
 * This is {@link WebAuthnStartAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class,
    properties = "cas.authn.mfa.web-authn.core.qr-code-authentication-enabled=true")
class WebAuthnStartAuthenticationActionTests {
    private static final String USER = UUID.randomUUID().toString();

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_START_AUTHENTICATION)
    private Action webAuthnStartAuthenticationAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    void initialize() {
        webAuthnCredentialRepository.removeAllRegistrations(USER);
    }

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);
        val authn = RegisteredServiceTestUtils.getAuthentication(USER);
        WebUtils.putAuthentication(authn, context);
        var result = webAuthnStartAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(),
            CredentialRegistration.builder()
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                    .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());
        result = webAuthnStartAuthenticationAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());

        assertTrue(context.getFlowScope().contains("QRCode"));
        assertTrue(context.getFlowScope().contains("QRCodeUri"));
        assertTrue(context.getFlowScope().contains("QRCodeTicket"));
        assertNotNull(WebUtils.getPrincipalFromRequestContext(context));
    }

}
