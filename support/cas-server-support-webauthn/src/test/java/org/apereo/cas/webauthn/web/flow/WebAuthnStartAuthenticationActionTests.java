package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;

import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnStartAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
public class WebAuthnStartAuthenticationActionTests {
    private static final String USER = UUID.randomUUID().toString();

    @Autowired
    @Qualifier("webAuthnStartAuthenticationAction")
    private Action webAuthnStartAuthenticationAction;

    @Autowired
    @Qualifier("webAuthnCredentialRepository")
    private WebAuthnCredentialRepository webAuthnCredentialRepository;

    @Autowired
    @Qualifier("webAuthnMultifactorAuthenticationProvider")
    private MultifactorAuthenticationProvider webAuthnMultifactorAuthenticationProvider;

    @BeforeEach
    public void initialize() {
        webAuthnCredentialRepository.removeAllRegistrations(USER);
    }

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        WebUtils.putMultifactorAuthenticationProviderIdIntoFlowScope(context, webAuthnMultifactorAuthenticationProvider);
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

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
    }

}
