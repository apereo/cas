package org.apereo.cas.webauthn.web;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.security.BaseWebSecurityTests;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.WebAuthnUtils;
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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.execution.Action;
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
        val request = context.getHttpServletRequest();
        val mobileSession = new MockHttpSession();
        val mainSession = new MockHttpSession();
        request.setSession(mobileSession);
        val ticket = getQRCodeTicket(context, authn);
        val csrfToken = createCsrfToken(context);
        val userHandle = webAuthnCredentialRepository.getUserHandleForUsername(authn.getPrincipal().getId()).orElseThrow();
        val sessionId = webAuthnSessionManager.createSession(request, userHandle);
        val mv = mvc.perform(post(BASE_ENDPOINT)
                .cookie(context.getHttpServletResponse().getCookie("XSRF-TOKEN"))
                .queryParam("token", sessionId.getBase64Url())
                .queryParam("ticket", ticket.getId())
                .queryParam("principal", authn.getPrincipal().getId())
                .header("X-CSRF-TOKEN", csrfToken.getToken())
                .session(mobileSession)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertTrue((Boolean) mv.getModel().get("success"));
        assertNotNull(mv.getModel().get("principal"));
        assertTrue(webAuthnSessionManager.getSession(request, sessionId).isEmpty());

        val statusResponse = mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", ticket.getId()).session(mainSession))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();
        val returnedSessionToken = WebAuthnUtils.getObjectMapper().readTree(statusResponse.getContentAsString())
            .get("sessionToken").asText();
        assertNotEquals(sessionId.getBase64Url(), returnedSessionToken);
        val mainRequest = new MockHttpServletRequest();
        mainRequest.setSession(mainSession);
        assertTrue(webAuthnSessionManager.getSession(mainRequest, ByteArray.fromBase64Url(returnedSessionToken)).isPresent());
        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", UUID.randomUUID().toString()).session(mainSession))
            .andExpect(status().isBadRequest());
    }

    @Test
    void verifyQRCodeRejectsSessionForDifferentPrincipal() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val request = context.getHttpServletRequest();
        val mobileSession = new MockHttpSession();
        request.setSession(mobileSession);
        val ticket = getQRCodeTicket(context, authn);
        val csrfToken = createCsrfToken(context);

        val otherAuthentication = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val otherUserHandle = registerCredential(otherAuthentication);
        val sessionId = webAuthnSessionManager.createSession(request, otherUserHandle);
        val mv = mvc.perform(post(BASE_ENDPOINT)
                .cookie(context.getHttpServletResponse().getCookie("XSRF-TOKEN"))
                .queryParam("token", sessionId.getBase64Url())
                .queryParam("ticket", ticket.getId())
                .queryParam("principal", authn.getPrincipal().getId())
                .header("X-CSRF-TOKEN", csrfToken.getToken())
                .session(mobileSession))
            .andExpect(status().isOk())
            .andReturn()
            .getModelAndView();
        assertFalse((Boolean) mv.getModel().get("success"));
    }

    @Test
    void verifyQRCodeTicketStatusNotReady() throws Exception {
        val context = MockRequestContext.create(webApplicationContext);
        val authn = RegisteredServiceTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticket = getQRCodeTicket(context, authn);
        mvc.perform(get(BASE_ENDPOINT + "/{ticket}/status", ticket.getId()))
            .andExpect(status().isUnprocessableContent());
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
        assertNotNull(mv.getModel().get("_csrf"));
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
        registerCredential(authentication);
        webAuthnStartAuthenticationAction.execute(context);
        return context.getFlowScope().get("QRCodeTicket", TransientSessionTicket.class);
    }

    private ByteArray registerCredential(final Authentication authentication) {
        val userHandle = SessionManager.generateRandom(32);
        webAuthnCredentialRepository.addRegistrationByUsername(authentication.getPrincipal().getId(),
            CredentialRegistration.builder()
                .userIdentity(UserIdentity.builder()
                    .name(authentication.getPrincipal().getId())
                    .displayName("CAS")
                    .id(userHandle)
                    .build())
                .registrationTime(Instant.now(Clock.systemUTC()))
                .credential(RegisteredCredential.builder()
                    .credentialId(SessionManager.generateRandom(32))
                    .userHandle(userHandle)
                    .publicKeyCose(SessionManager.generateRandom(8))
                    .build())
                .build());
        return userHandle;
    }
}
