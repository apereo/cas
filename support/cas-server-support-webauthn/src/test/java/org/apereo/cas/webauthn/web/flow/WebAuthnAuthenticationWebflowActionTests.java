package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnCredential;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import com.yubico.core.SessionManager;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.DefaultMessageContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link WebAuthnAuthenticationWebflowActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
public class WebAuthnAuthenticationWebflowActionTests {
    @Autowired
    @Qualifier("webAuthnAuthenticationWebflowAction")
    private Action webAuthnAuthenticationWebflowAction;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnSessionManager")
    private SessionManager webAuthnSessionManager;

    private static RequestContext getRequestContext() {
        val context = new MockRequestContext();
        val messageContext = (DefaultMessageContext) context.getMessageContext();
        messageContext.setMessageSource(mock(MessageSource.class));
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        return context;
    }

    @Test
    public void verifyFailsNoAuthn() throws Exception {
        val context = getRequestContext();
        WebUtils.putCredential(context, new WebAuthnCredential(EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8))));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());
        val result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    public void verifyFailsNoReg() throws Exception {
        val context = getRequestContext();
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        WebUtils.putAuthentication(authn, context);
        WebUtils.putCredential(context, new WebAuthnCredential(EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8))));
        val result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());
    }

    @Test
    public void verifyToken() throws Exception {
        val context = getRequestContext();
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val authn = RegisteredServiceTestUtils.getAuthentication("casuser");
        WebUtils.putAuthentication(authn, context);
        WebUtils.putCredential(context, new WebAuthnCredential(EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8))));
        webAuthnCredentialRepository.addRegistrationByUsername(authn.getPrincipal().getId(),
            CredentialRegistration.builder()
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(authn.getPrincipal().getId()))
                    .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());
        var result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_ERROR, result.getId());

        val token = EncodingUtils.encodeBase64(RandomUtils.randomAlphabetic(8));
        val sessionId = webAuthnSessionManager.createSession(ByteArray.fromBase64(token));

        val builder = mock(AuthenticationResultBuilder.class);
        when(builder.getInitialAuthentication()).thenReturn(Optional.of(authn));
        when(builder.collect(any(Authentication.class))).thenReturn(builder);
        WebUtils.putAuthenticationResultBuilder(builder, context);

        WebUtils.putCredential(context, new WebAuthnCredential(sessionId.toJsonString()));
        result = webAuthnAuthenticationWebflowAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, result.getId());
    }
}
