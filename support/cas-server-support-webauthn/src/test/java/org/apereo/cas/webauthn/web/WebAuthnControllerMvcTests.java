package org.apereo.cas.webauthn.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.security.BaseWebSecurityTests;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.webauthn.web.flow.BaseWebAuthnWebflowTests;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.HttpServletRequest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This is {@link WebAuthnControllerMvcTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@SpringBootTest(classes = {
    BaseWebSecurityTests.SharedTestConfiguration.class,
    BaseWebAuthnWebflowTests.SharedTestConfiguration.class
},
    properties = {
        "management.endpoints.access.default=UNRESTRICTED",
        "management.endpoints.web.exposure.include=*",

        "spring.security.user.name=s#kiooritea",
        "spring.security.user.password=p@$$W0rd",

        "cas.monitor.endpoints.endpoint.env.access=AUTHENTICATED",
        "cas.monitor.endpoints.endpoint.info.access=ANONYMOUS"
    }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Tag("MFAProvider")
@ExtendWith(CasTestExtension.class)
class WebAuthnControllerMvcTests {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    @Qualifier("webAuthnCsrfTokenRepository")
    private CsrfTokenRepository csrfTokenRepository;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_POPULATE_SECURITY_CONTEXT)
    private Action populateSecurityContextAction;

    @Autowired
    private SecurityProperties securityProperties;

    private MockMvc mvc;

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
    void verifyRegistrationEndpoint() throws Throwable {
        val endpoint = WebAuthnController.WEBAUTHN_ENDPOINT_REGISTER;

        /* Without CSRF token, we must fail */
        executeRequest(endpoint, new MockHttpServletRequest(), new MockHttpServletResponse(), true, HttpStatus.SC_FORBIDDEN);

        /* With CSRF token but without authentication context we continue to fail  */
        val csrfResult = fetchAndStoreCsrfToken();
        executeRequest(endpoint, csrfResult.getRequest(), csrfResult.getResponse(), true, HttpStatus.SC_FORBIDDEN);

        /* Authenticated requests must fail because they do not have the right role */
        val result = executeRequest(endpoint, csrfResult.getRequest(), csrfResult.getResponse(), true, HttpStatus.SC_FORBIDDEN);

        /* Authorization should now pass and reach the endpoint */
        populateSecurityContext(result);
        /* Authenticated requests with the right role should pass */
        executeRequest(endpoint, csrfResult.getRequest(), csrfResult.getResponse(), false, HttpStatus.SC_BAD_REQUEST);

        /* Ensure security context does not interfere with actuator endpoint security */
        mvc.perform(get("/cas/actuator/env"))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/cas/actuator/env")
                .with(httpBasic("hello", "world")))
            .andExpect(status().isUnauthorized());
        mvc.perform(get("/cas/actuator/env")
                .with(httpBasic(securityProperties.getUser().getName(), securityProperties.getUser().getPassword())))
            .andExpect(status().isOk());
    }

    @Test
    void verifyAuthenticationEndpoint() throws Throwable {
        executeRequest(WebAuthnController.WEBAUTHN_ENDPOINT_AUTHENTICATE, new MockHttpServletRequest(), new MockHttpServletResponse(), false, HttpStatus.SC_FORBIDDEN);
        executeRequest(WebAuthnController.WEBAUTHN_ENDPOINT_AUTHENTICATE, new MockHttpServletRequest(), new MockHttpServletResponse(), true, HttpStatus.SC_FORBIDDEN);

        val csrfResult = fetchAndStoreCsrfToken();
        /*
          Authentication should pass but the user is not registered.
         */
        executeRequest(WebAuthnController.WEBAUTHN_ENDPOINT_AUTHENTICATE, csrfResult.getRequest(), csrfResult.getResponse(), true, HttpStatus.SC_BAD_REQUEST);
    }

    private void populateSecurityContext(final MvcResult result) throws Exception {
        val requestContext = MockRequestContext.create(webApplicationContext);
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), result.getRequest(), result.getResponse()));
        WebUtils.putAuthentication(RegisteredServiceTestUtils.getAuthentication(), requestContext);
        populateSecurityContextAction.execute(requestContext);
    }

    private MvcResult executeRequest(final String endpoint,
                                     final HttpServletRequest request,
                                     final MockHttpServletResponse response,
                                     final boolean withBasicAuth,
                                     final int expectedStatus) throws Exception {
        val csrfToken = getCsrfToken(request);
        var builder = post("/cas/" + WebAuthnController.BASE_ENDPOINT_WEBAUTHN + endpoint)
            .session((MockHttpSession) request.getSession());
        val cookies = response.getCookies();
        if (cookies != null && cookies.length > 0) {
            builder = builder.cookie(cookies);
        }
        if (withBasicAuth) {
            builder = builder.with(httpBasic(securityProperties.getUser().getName(),
                securityProperties.getUser().getPassword()));
        }

        return mvc.perform(builder.header("X-CSRF-TOKEN", csrfToken != null ? csrfToken.getToken() : StringUtils.EMPTY))
            .andExpect(status().is(expectedStatus))
            .andReturn();
    }

    private static CsrfToken getCsrfToken(final HttpServletRequest request) {
        val attribute = request.getAttribute(DeferredCsrfToken.class.getName());
        return attribute != null ? ((DeferredCsrfToken) attribute).get() : null;
    }

    private MvcResult fetchAndStoreCsrfToken() throws Exception {
        var csrfResult = mvc.perform(get("/cas/actuator/info"))
            .andExpect(status().isOk())
            .andReturn();
        val csrfToken = getCsrfToken(csrfResult.getRequest());
        csrfTokenRepository.saveToken(csrfToken, csrfResult.getRequest(), csrfResult.getResponse());
        return csrfResult;
    }
}
