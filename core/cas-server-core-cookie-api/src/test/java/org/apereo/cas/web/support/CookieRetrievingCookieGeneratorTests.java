package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public class CookieRetrievingCookieGeneratorTests {
    @Test
    public void verifyCookieValueByHeader() {
        val gen = new CookieRetrievingCookieGenerator("cas", "/", 1000, true, "example.org", true);
        val request = new MockHttpServletRequest();
        request.addHeader(gen.getCookieName(), "CAS-Cookie-Value");
        val cookie = gen.retrieveCookieValue(request);
        assertNotNull(cookie);
        assertEquals("CAS-Cookie-Value", cookie);
    }

    @Test
    public void verifyCookieForRememberMeByAuthnRequest() {
        val gen = new CookieRetrievingCookieGenerator("cas", "/", 1000, true, "example.org", true);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true");
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(context, "CAS-Cookie-Value");
        assertTrue(response.getCookies().length > 0);
    }

    @Test
    public void verifyCookieForRememberMeByRequestContext() {
        val gen = new CookieRetrievingCookieGenerator("cas", "/", 1000, true, "example.org", true);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true"));
        WebUtils.putAuthentication(authn, context);
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(context, "CAS-Cookie-Value");
        assertTrue(response.getCookies().length > 0);
    }
}
