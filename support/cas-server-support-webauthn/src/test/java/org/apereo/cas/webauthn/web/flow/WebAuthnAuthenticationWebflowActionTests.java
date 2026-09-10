package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import com.yubico.core.SessionManager;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebAuthnAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
class WebAuthnAuthenticationWebflowActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WEBAUTHN_AUTHENTICATION_WEBFLOW)
    private Action webAuthnAuthenticationWebflowAction;

    @Autowired
    @Qualifier(WebAuthnCredentialRepository.BEAN_NAME)
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier(SessionManager.BEAN_NAME)
    private SessionManager webAuthnSessionManager;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @AfterEach
    void cleanup() {
        RequestContextHolder.resetRequestAttributes();
        ClientInfoHolder.clear();
    }

    @Test
    void verifyFailsNoAuthn() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putCredential(context, new WebAuthnCredential(EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8))));
        val result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyFailsNoReg() throws Throwable {
        val context = MockRequestContext.create(applicationContext);

        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        WebUtils.putAuthentication(authn, context);
        WebUtils.putCredential(context, new WebAuthnCredential(EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8))));
        val result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    void verifyToken() throws Throwable {
        ClientInfoHolder.setClientInfo(new ClientInfo());
        val context = MockRequestContext.create(applicationContext);
        val request = context.getHttpServletRequest();
        request.setSession(new MockHttpSession());

        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val userHandle = SessionManager.generateRandom(32);
        WebUtils.putAuthentication(authn, context);
        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(),
            CredentialRegistration.builder()
                .userIdentity(UserIdentity.builder()
                    .name(authn.getPrincipal().getId())
                    .displayName(authn.getPrincipal().getId())
                    .id(userHandle)
                    .build())
                .credential(RegisteredCredential.builder()
                    .credentialId(SessionManager.generateRandom(32))
                    .userHandle(userHandle)
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, context.getHttpServletResponse()));
        try {
            WebUtils.putCredential(context, new WebAuthnCredential(SessionManager.generateRandom(32).getBase64Url()));
            var result = webAuthnAuthenticationWebflowAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

            val sessionId = webAuthnSessionManager.createSession(request, userHandle);
            WebUtils.putCredential(context, new WebAuthnCredential(sessionId.getBase64Url()));
            result = webAuthnAuthenticationWebflowAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());

            result = webAuthnAuthenticationWebflowAction.execute(context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
        } finally {
            RequestContextHolder.resetRequestAttributes();
            ClientInfoHolder.clear();
        }
    }
}
