package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Simple")
public class CookieRetrievingCookieGeneratorTests {

    private static CookieGenerationContext getCookieGenerationContext() {
        return CookieGenerationContext.builder()
            .name("cas")
            .path("/")
            .maxAge(1000)
            .comment("CAS Cookie")
            .domain("example.org")
            .secure(true)
            .httpOnly(true)
            .build();
    }

    @Test
    public void verifyCookieValueByHeader() {
        val context = getCookieGenerationContext();

        val gen = new CookieRetrievingCookieGenerator(context);
        val request = new MockHttpServletRequest();
        request.addHeader(context.getName(), "CAS-Cookie-Value");
        val cookie = gen.retrieveCookieValue(request);
        assertNotNull(cookie);
        assertEquals("CAS-Cookie-Value", cookie);
    }

    @Test
    public void verifyCookieForRememberMeByAuthnRequest() {
        val ctx = getCookieGenerationContext();

        val gen = new CookieRetrievingCookieGenerator(ctx);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME, "true");
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(request, response, CookieRetrievingCookieGenerator.isRememberMeAuthentication(context), "CAS-Cookie-Value");
        val cookie = response.getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals(ctx.getRememberMeMaxAge(), cookie.getMaxAge());
    }

    @Test
    public void verifyCookieForRememberMeByRequestContext() {
        val ctx = getCookieGenerationContext();

        val gen = new CookieRetrievingCookieGenerator(ctx);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, CollectionUtils.wrap(Boolean.TRUE)));
        WebUtils.putAuthentication(authn, context);
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(request, response, CookieRetrievingCookieGenerator.isRememberMeAuthentication(context), "CAS-Cookie-Value");
        val cookie = response.getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals(ctx.getRememberMeMaxAge(), cookie.getMaxAge());
    }
}
