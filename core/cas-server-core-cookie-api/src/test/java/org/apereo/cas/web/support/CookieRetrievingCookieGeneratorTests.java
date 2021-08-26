package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Cookie")
public class CookieRetrievingCookieGeneratorTests {

    private static CookieGenerationContext getCookieGenerationContext(final String path) {
        return CookieGenerationContext.builder()
            .name("cas")
            .path(path)
            .maxAge(1000)
            .comment("CAS Cookie")
            .domain("example.org")
            .secure(true)
            .httpOnly(true)
            .build();
    }

    private static CookieGenerationContext getCookieGenerationContext() {
        return getCookieGenerationContext("/");
    }

    @Test
    public void verifyCookiePathNotModified() {
        val request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var gen1 = new CookieRetrievingCookieGenerator(getCookieGenerationContext("/custom/path/"));
        var cookie1 = gen1.addCookie(request, response, "some-value");
        assertEquals("/custom/path/", cookie1.getPath());

        gen1 = new CookieRetrievingCookieGenerator(getCookieGenerationContext(StringUtils.EMPTY));
        cookie1 = gen1.addCookie(request, response, "some-value");
        assertEquals("/", cookie1.getPath());
    }

    @Test
    public void verifyRemoveAllCookiesByName() {
        val request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        val gen1 = new CookieRetrievingCookieGenerator(getCookieGenerationContext());
        val cookie1 = gen1.addCookie(request, response, "some-value");

        val gen2 = new CookieRetrievingCookieGenerator(getCookieGenerationContext("/cas"));
        val cookie2 = gen2.addCookie(request, response, "some-value");

        val gen3 = new CookieRetrievingCookieGenerator(getCookieGenerationContext("/cas/"));
        val cookie3 = gen3.addCookie(request, response, "some-value");

        request.setCookies(cookie1, cookie2, cookie3);
        response = new MockHttpServletResponse();
        gen1.removeAll(request, response);
        assertEquals(9, response.getCookies().length);
        assertTrue(Arrays.stream(response.getCookies()).allMatch(c -> c.getMaxAge() == 0));
    }

    @Test
    public void verifyExistingCookieInResponse() {
        val context = getCookieGenerationContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val gen = new CookieRetrievingCookieGenerator(context);

        var cookie = gen.addCookie(request, response, "some-value");
        assertNotNull(cookie);
        var headers = response.getHeaders("Set-Cookie");
        assertEquals(1, headers.size());
        assertTrue(headers.get(0).contains(context.getName() + '=' + cookie.getValue()));

        cookie = gen.addCookie(request, response, "updated-value");
        assertNotNull(cookie);
        headers = response.getHeaders("Set-Cookie");
        assertEquals(1, headers.size());
        assertTrue(headers.get(0).contains(context.getName() + '=' + cookie.getValue()));
    }

    @Test
    public void verifyOtherSetCookieHeaderIsNotDiscarded() {
        val context = getCookieGenerationContext();

        val gen = new CookieRetrievingCookieGenerator(context);
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        response.addHeader("Set-Cookie", gen.getCookieName() + "=some-cookie-value");
        response.addHeader("Set-Cookie", "OtherCookie=other-cookie-value");
        var headers = response.getHeaders("Set-Cookie");
        assertEquals(2, headers.size());
        val cookie = gen.addCookie(request, response, "some-value");
        assertNotNull(cookie);
        assertEquals("some-value", cookie.getValue());
        var headersAfter = response.getHeaders("Set-Cookie");
        assertEquals(2, headersAfter.size());
        val headerValuesAfter = response.getHeaderValues("Set-Cookie").stream()
            .map(String.class::cast)
            .map(header -> Arrays.stream(header.split(";")).iterator().next())
            .collect(Collectors.toSet());
        assertEquals(headerValuesAfter, CollectionUtils.wrapSet(cookie.getName() + "=some-value", "OtherCookie=other-cookie-value"));
    }

    @Test
    public void verifyCookieValueMissing() {
        val context = getCookieGenerationContext();
        context.setName(StringUtils.EMPTY);

        val gen = new CookieRetrievingCookieGenerator(context);
        val request = new MockHttpServletRequest();
        request.addHeader(context.getName(), "CAS-Cookie-Value");
        val cookie = gen.retrieveCookieValue(request);
        assertNull(cookie);
    }

    @Test
    public void verifyCookieSameSiteLax() {
        val ctx = getCookieGenerationContext();
        ctx.setSameSitePolicy("lax");

        val gen = new CookieRetrievingCookieGenerator(ctx);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(request, response, false, "CAS-Cookie-Value");
        val cookie = (MockCookie) response.getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals("Lax", cookie.getSameSite());
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
