package org.apereo.cas.web.support;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
import org.apereo.cas.configuration.model.support.cookie.PinnableCookieProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.spring.DirectObjectProvider;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import org.apereo.cas.web.support.gen.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.mgmr.DefaultCasCookieValueManager;
import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import java.util.Arrays;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link CookieRetrievingCookieGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Tag("Cookie")
class CookieRetrievingCookieGeneratorTests {

    private static CookieGenerationContext getCookieGenerationContext(final String path) {
        return CookieGenerationContext
            .builder()
            .name("cas")
            .path(path)
            .maxAge(1000)
            .domain("example.org")
            .secure(true)
            .httpOnly(true)
            .build();
    }

    private static CookieGenerationContext getCookieGenerationContext() {
        return getCookieGenerationContext("/");
    }

    @Test
    void verifyCookiePathNotModified() throws Throwable {
        val request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        var gen1 = CookieUtils.buildCookieRetrievingGenerator(getCookieGenerationContext("/custom/path/"));
        var cookie1 = gen1.addCookie(request, response, "some-value");
        assertEquals("/custom/path/", cookie1.getPath());

        gen1 = CookieUtils.buildCookieRetrievingGenerator(getCookieGenerationContext(StringUtils.EMPTY));
        cookie1 = gen1.addCookie(request, response, "some-value");
        assertEquals("/", cookie1.getPath());
    }

    @Test
    void verifyRemoveAllCookiesByName() throws Throwable {
        val request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        val gen1 = CookieUtils.buildCookieRetrievingGenerator(getCookieGenerationContext());
        val cookie1 = gen1.addCookie(request, response, "some-value");

        val gen2 = CookieUtils.buildCookieRetrievingGenerator(getCookieGenerationContext("/cas"));
        val cookie2 = gen2.addCookie(request, response, "some-value");

        val gen3 = CookieUtils.buildCookieRetrievingGenerator(getCookieGenerationContext("/cas/"));
        val cookie3 = gen3.addCookie(request, response, "some-value");

        request.setCookies(cookie1, cookie2, cookie3);
        response = new MockHttpServletResponse();
        gen1.removeAll(request, response);
        assertEquals(3, response.getCookies().length);
        assertTrue(Arrays.stream(response.getCookies()).allMatch(cookie -> cookie.getMaxAge() == 0));
    }

    @Test
    void verifyExistingCookieInResponse() throws Throwable {
        val context = getCookieGenerationContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val gen = CookieUtils.buildCookieRetrievingGenerator(context);

        var cookie = gen.addCookie(request, response, "some-value");
        assertNotNull(cookie);
        var headers = response.getHeaders("Set-Cookie");
        assertEquals(1, headers.size());
        assertTrue(headers.getFirst().contains(context.getName() + '=' + cookie.getValue()));

        cookie = gen.addCookie(request, response, "updated-value");
        assertNotNull(cookie);
        headers = response.getHeaders("Set-Cookie");
        assertEquals(1, headers.size());
        assertTrue(headers.getFirst().contains(context.getName() + '=' + cookie.getValue()));
    }

    @Test
    void verifyOtherSetCookieHeaderIsNotDiscarded() throws Throwable {
        val context = getCookieGenerationContext();

        val gen = CookieUtils.buildCookieRetrievingGenerator(context);
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
    void verifyCookieValueMissing() throws Throwable {
        val context = getCookieGenerationContext();
        context.setName(StringUtils.EMPTY);

        val gen = CookieUtils.buildCookieRetrievingGenerator(context);
        val request = new MockHttpServletRequest();
        request.addHeader(context.getName(), "CAS-Cookie-Value");
        val cookie = gen.retrieveCookieValue(request);
        assertNull(cookie);
    }

    @Test
    void verifyCookieSameSiteLax() throws Throwable {
        val ctx = getCookieGenerationContext();
        ctx.setSameSitePolicy("lax");

        val gen = CookieUtils.buildCookieRetrievingGenerator(new DefaultCasCookieValueManager(CipherExecutor.noOp(),
            new DirectObjectProvider<>(mock(GeoLocationService.class)),
            DefaultCookieSameSitePolicy.INSTANCE, new PinnableCookieProperties().setPinToSession(false)), ctx);
        val context = MockRequestContext.create();
        gen.addCookie(context.getHttpServletRequest(), context.getHttpServletResponse(), false, "CAS-Cookie-Value");
        val cookie = (MockCookie) context.getHttpServletResponse().getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals("Lax", cookie.getSameSite());
    }

    @Test
    void verifyCookieValueByHeader() throws Throwable {
        val context = getCookieGenerationContext();

        val gen = CookieUtils.buildCookieRetrievingGenerator(context);
        val request = new MockHttpServletRequest();
        request.addHeader(context.getName(), "CAS-Cookie-Value");
        val cookie = gen.retrieveCookieValue(request);
        assertNotNull(cookie);
        assertEquals("CAS-Cookie-Value", cookie);
    }

    @Test
    void verifyCookieForRememberMeByAuthnRequest() throws Throwable {
        val ctx = getCookieGenerationContext();

        val gen = CookieUtils.buildCookieRetrievingGenerator(ctx);
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
    void verifyCookieForRememberMeByRequestContext() throws Throwable {
        val ctx = getCookieGenerationContext();

        val gen = CookieUtils.buildCookieRetrievingGenerator(ctx);
        val context = MockRequestContext.create();

        val authn = CoreAuthenticationTestUtils.getAuthentication("casuser",
            CollectionUtils.wrap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, CollectionUtils.wrap(Boolean.TRUE)));
        WebUtils.putAuthentication(authn, context);
        WebUtils.putRememberMeAuthenticationEnabled(context, Boolean.TRUE);

        gen.addCookie(context.getHttpServletRequest(), context.getHttpServletResponse(),
            CookieRetrievingCookieGenerator.isRememberMeAuthentication(context), "CAS-Cookie-Value");
        val cookie = context.getHttpServletResponse().getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals(ctx.getRememberMeMaxAge(), cookie.getMaxAge());
    }
}
