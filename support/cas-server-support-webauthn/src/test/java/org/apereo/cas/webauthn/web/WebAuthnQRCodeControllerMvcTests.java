package org.apereo.cas.webauthn.web;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.security.BaseWebSecurityTests;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.storage.WebAuthnCredentialRepository;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import com.yubico.core.SessionManager;
import com.yubico.data.CredentialRegistration;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.UserIdentity;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.execution.Action;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link WebAuthnQRCodeControllerMvcTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseWebSecurityTests.SharedTestConfiguration.class,
    BaseWebAuthnWebflowTests.SharedTestConfiguration.class
}, properties = "cas.authn.mfa.web-authn.core.qr-code-authentication-enabled=true",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class WebAuthnQRCodeControllerMvcTests {
    public static final String BASE_ENDPOINT = "/cas" + BaseWebAuthnController.BASE_ENDPOINT_WEBAUTHN + WebAuthnQRCodeController.ENDPOINT_QR_VERIFY;
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("webAuthnCsrfTokenRepository")
    private CsrfTokenRepository csrfTokenRepository;
    
    private MockMvc mvc;

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
    @Qualifier(SessionManager.BEAN_NAME)
    private SessionManager webAuthnSessionManager;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .defaultRequest(get("/")
                .contextPath("/cas")
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .contentType(MediaType.APPLICATION_JSON))
            .build();
    }

    @Test
    void verifyInvalidTicket() throws Throwable {
        val uriTemplate = "/cas" + BaseWebAuthnController.BASE_ENDPOINT_WEBAUTHN + WebAuthnQRCodeController.ENDPOINT_QR_VERIFY + "/{ticket}";
        val mv = mvc.perform(get(uriTemplate, UUID.randomUUID().toString()))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertFalse((Boolean) mv.getModel().get("success"));
    }

    @Test
    void verifyQRCodeSuccessfully() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val request = new MockHttpServletRequest();
        val ticket = getQRCodeTicket(context, authn);
        val csrfToken = createCsrfToken(context);
        val sessionId = webAuthnSessionManager.createSession(request, ByteArray.fromBase64Url(authn.getPrincipal().getId()));
        val mv = mvc.perform(post(BASE_ENDPOINT)
                .cookie(context.getHttpServletResponse().getCookie("XSRF-TOKEN"))
                .queryParam("token", sessionId.getBase64Url())
                .queryParam("ticket", ticket.getId())
                .queryParam("principal", authn.getPrincipal().getId())
                .header("X-CSRF-TOKEN", csrfToken.getToken())
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertTrue((Boolean) mv.getModel().get("success"));
        assertNotNull(mv.getModel().get("principal"));

        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", ticket.getId()))
            .andExpect(status().isOk());
        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", UUID.randomUUID().toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyQRCodeTicketStatusNotReady() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticket = getQRCodeTicket(context, authn);
        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", ticket.getId()))
            .andExpect(status().isUnprocessableEntity());
        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", UUID.randomUUID().toString()))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyQRCodeWithInvalidSession() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val request = new MockHttpServletRequest();
        val ticket = getQRCodeTicket(context, authn);
        val csrfToken = createCsrfToken(context);
        val sessionToken = UUID.randomUUID().toString();
        val sessionId = webAuthnSessionManager.createSession(request, ByteArray.fromBase64Url(sessionToken));
        val mv = mvc.perform(post(BASE_ENDPOINT)
                .cookie(context.getHttpServletResponse().getCookie("XSRF-TOKEN"))
                .queryParam("token", sessionId.getBase64Url())
                .queryParam("ticket", ticket.getId())
                .queryParam("principal", authn.getPrincipal().getId())
                .header("X-CSRF-TOKEN", csrfToken.getToken())
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertFalse((Boolean) mv.getModel().get("success"));
    }

    @Test
    void verifyQRCodeWithoutSession() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticket = getQRCodeTicket(context, authn);
        val csrfToken = createCsrfToken(context);
        val sessionToken = UUID.randomUUID().toString();
        var mv = mvc.perform(post(BASE_ENDPOINT)
                .cookie(context.getHttpServletResponse().getCookie("XSRF-TOKEN"))
                .queryParam("token", sessionToken)
                .queryParam("ticket", ticket.getId())
                .queryParam("principal", authn.getPrincipal().getId())
                .header("X-CSRF-TOKEN", csrfToken.getToken())
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertFalse((Boolean) mv.getModel().get("success"));
    }

    @Test
    void verifyAuthenticationStart() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticket = getQRCodeTicket(context, authn);
        var mv = mvc.perform(get(BASE_ENDPOINT + "/{ticket}", ticket.getId()))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertNotNull(mv);
        assertNotNull(mv.getModel());
        assertNotNull(mv.getModel().get("QRCodeTicket"));
        assertNotNull(mv.getModel().get("principal"));
        assertTrue((Boolean) mv.getModel().get("QRCodeAuthentication"));
    }

    private CsrfToken createCsrfToken(final MockRequestContext context) {
        val csrfToken = csrfTokenRepository.generateToken(context.getHttpServletRequest());
        csrfTokenRepository.saveToken(csrfToken, context.getHttpServletRequest(), context.getHttpServletResponse());
        context.setRequestCookiesFromResponse();
        return csrfToken;
    }

    private TransientSessionTicket getQRCodeTicket(final MockRequestContext context, final Authentication authentication) throws Exception {
        WebUtils.putAuthentication(authentication, context);
        MultifactorAuthenticationWebflowUtils.putMultifactorAuthenticationProvider(context, webAuthnMultifactorAuthenticationProvider);
        webAuthnCredentialRepository.addRegistrationByUsername(authentication.getPrincipal().getId(),
            CredentialRegistration.builder()
                .userIdentity(UserIdentity.builder()
                    .name(authentication.getPrincipal().getId())
                    .displayName("CAS")
                    .id(ByteArray.fromBase64Url(authentication.getPrincipal().getId()))
                    .build())
                .registrationTime(Instant.now(Clock.systemUTC()))
                .credential(RegisteredCredential.builder()
                    .credentialId(ByteArray.fromBase64Url(authentication.getPrincipal().getId()))
                    .userHandle(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .publicKeyCose(ByteArray.fromBase64Url(RandomUtils.randomAlphabetic(8)))
                    .build())
                .build());
        webAuthnStartAuthenticationAction.execute(context);
        return context.getFlowScope().get("QRCodeTicket", TransientSessionTicket.class);
    }
}
