package org.apereo.cas.web.support;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.util.CollectionUtils;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorTests}.
 *
 * @author sbearcsiro
 * @since 5.3.0
 */
public class CookieRetrievingCookieGeneratorTests {

    private static final String CAS_COOKIE_VALUE = "CAS-Cookie-Value";
    private static final String EXAMPLE_ORG = "example.org";
    private static final String CAS_COOKIE_NAME = "cas";

    @Test
    public void verifyCookieValueByHeader() {
        final CookieRetrievingCookieGenerator gen = new CookieRetrievingCookieGenerator(CAS_COOKIE_NAME, "/", 1000, true, EXAMPLE_ORG, true);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(gen.getCookieName(), CAS_COOKIE_VALUE);
        final String cookie = gen.retrieveCookieValue(request);
        assertNotNull(cookie);
        assertEquals(CAS_COOKIE_VALUE, cookie);
    }

    @Test
    public void verifyCookieForRememberMeByAuthnRequest() {
        final CookieRetrievingCookieGenerator gen = new CookieRetrievingCookieGenerator(CAS_COOKIE_NAME, "/", 1000, true, EXAMPLE_ORG, true);
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true");
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(context, CAS_COOKIE_VALUE);
        assertTrue(response.getCookies().length > 0);
    }

    @Test
    public void verifyCookieForRememberMeByRequestContext() {
        final int rememberMeMaxAge = 99999;
        final CookieRetrievingCookieGenerator gen = new CookieRetrievingCookieGenerator(CAS_COOKIE_NAME, "/", 1000, true, EXAMPLE_ORG, new NoOpCookieValueManager(), rememberMeMaxAge, true);
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authn = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser"),
            CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, CollectionUtils.wrap(Boolean.TRUE)));
        WebUtils.putAuthentication(authn, context);
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(context, CAS_COOKIE_VALUE);
        final Cookie cookie = response.getCookie(CAS_COOKIE_NAME);
        assertNotNull(cookie);
        assertEquals(rememberMeMaxAge, cookie.getMaxAge());
    }

    @Test
    public void verifyCookieForRememberMeByRequestContextLegacyAttribute() {
        final int rememberMeMaxAge = 99999;
        final CookieRetrievingCookieGenerator gen = new CookieRetrievingCookieGenerator(CAS_COOKIE_NAME, "/", 1000, true, EXAMPLE_ORG, new NoOpCookieValueManager(), rememberMeMaxAge, true);
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authn = CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal("casuser"),
                CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE));
        WebUtils.putAuthentication(authn, context);
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        final MockHttpServletResponse response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(context, CAS_COOKIE_VALUE);
        final Cookie cookie = response.getCookie(CAS_COOKIE_NAME);
        assertNotNull(cookie);
        assertEquals(rememberMeMaxAge, cookie.getMaxAge());
    }
}
