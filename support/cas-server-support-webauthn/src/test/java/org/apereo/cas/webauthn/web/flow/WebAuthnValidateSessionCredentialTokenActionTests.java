package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

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
        "cas.authn.mfa.web-authn.core.allowed-origins=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.application-id=https://localhost:8443",
        "cas.authn.mfa.web-authn.core.relying-party-name=CAS WebAuthn Demo",
        "cas.authn.mfa.web-authn.core.relying-party-id=example.org",
        "cas.authn.mfa.web-authn.core.allow-primary-authentication=true"
    })
public class WebAuthnValidateSessionCredentialTokenActionTests {
    private static final String SAMPLE_TOKEN = "mO2ST2ZLIZCP6VmGDkiIX-_-VNXfOJQ6TjCwUFSCA3Y";

    @Autowired
    @Qualifier("webAuthnValidateSessionCredentialTokenAction")
    private Action webAuthnValidateSessionCredentialTokenAction;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnSessionManager")
    private SessionManager webAuthnSessionManager;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;
    
    @Test
    public void verifyMissingToken() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);
        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    public void verifyEmptySessionForToken() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter("token", SAMPLE_TOKEN);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);
        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    public void verifyNoUserForToken() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val token = webAuthnSessionManager.createSession(ByteArray.fromBase64Url(SAMPLE_TOKEN));
        request.addParameter("token", token.getBase64Url());

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);

        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, result.getId());
    }

    @Test
    public void verifySuccessAuthForToken() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val userHandle = ByteArray.fromBase64Url(SAMPLE_TOKEN);
        val token = webAuthnSessionManager.createSession(userHandle);
        request.addParameter("token", token.getBase64Url());

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

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);

        val result = webAuthnValidateSessionCredentialTokenAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINALIZE, result.getId());
        assertNotNull(WebUtils.getAuthentication(context));
    }

}
