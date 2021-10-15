package org.apereo.cas.adaptors.swivel;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.util.MockWebServer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.security.auth.login.FailedLoginException;

import static java.nio.charset.StandardCharsets.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.webflow.execution.RequestContextHolder.*;

/**
 * This is {@link SwivelAuthenticationHandlerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = BaseSwivelAuthenticationTests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.mfa.swivel.swivel-url=http://localhost:9191",
        "cas.authn.mfa.swivel.shared-secret=$ecret",
        "cas.authn.mfa.swivel.ignore-ssl-errors=true"
    })
@Tag("MFAProvider")
public class SwivelAuthenticationHandlerTests {
    @Autowired
    @Qualifier("swivelAuthenticationHandler")
    private AuthenticationHandler swivelAuthenticationHandler;

    @Test
    public void verifySupports() {
        assertFalse(swivelAuthenticationHandler.supports(
            CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword().getCredentialClass()));
        assertTrue(swivelAuthenticationHandler.supports(new SwivelTokenCredential("123456")));
    }

    @Test
    public void verifyAuthn() throws Exception {
        val data = "<?xml version=\"1.0\" ?>"
            + "<SASResponse secret=\"MyAdminAgent\" version=\"3.4\">"
            + "<Version>3.6</Version>\n"
            + "<Result>PASS</Result>\n"
            + "<SessionID>c7379ef1b41f90a4900548a75e13f62a</SessionID>"
            + "</SASResponse>";

        try (val webServer = new MockWebServer(9191,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val c = new SwivelTokenCredential("123456");
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            setRequestContext(context);
            assertNotNull(swivelAuthenticationHandler.authenticate(c));
        }
    }

    @Test
    public void verifyAuthnFails() throws Exception {
        val data = "<?xml version=\"1.0\" ?>"
            + "<SASResponse secret=\"MyAdminAgent\" version=\"3.4\">"
            + "<Version>3.6</Version>\n"
            + "<Result>FAIL</Result>\n"
            + "<SessionID>c7379ef1b41f90a4900548a75e13f62a</SessionID>"
            + "</SASResponse>";

        try (val webServer = new MockWebServer(9191,
            new ByteArrayResource(data.getBytes(UTF_8), "Output"), HttpStatus.OK)) {
            webServer.start();
            val c = new SwivelTokenCredential("123456");
            val context = new MockRequestContext();
            val request = new MockHttpServletRequest();
            val response = new MockHttpServletResponse();
            context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
            setRequestContext(context);
            ExternalContextHolder.setExternalContext(context.getExternalContext());
            WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
            setRequestContext(context);
            assertThrows(FailedLoginException.class, () -> swivelAuthenticationHandler.authenticate(c));
        }
    }
}
