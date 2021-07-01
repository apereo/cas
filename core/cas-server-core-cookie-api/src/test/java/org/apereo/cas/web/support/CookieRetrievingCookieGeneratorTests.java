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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            "Mozilla/5.0 (iPhone; CPU iPhone OS 13_0_0 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/13.0.0 Mobile/14A403 Safari/604.1.28",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 13_4_5 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) FxiOS/1.0 Mobile/12F69 Safari/600.1.4",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0_0 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/11.0.0 Mobile/14A403 Safari/604.1.28",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 11_4_5 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) CriOS/60.0.0.0 Mobile/14E5239e Safari/602.1",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_4) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/11.4.0 Safari/604.1.28",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/10.13.1 Safari/604.1.28",
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/50.0.0.0 Safari/537.36.0",
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/67.0.8.15 Safari/537.36.0",
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/91.0.0.0 Safari/537.36.0",
            "Mozilla/5.0 (Linux; Android 7.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/50.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 8.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/67.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 9.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/91.0.0.0 Mobile Safari/537.36"
    })
    public void verifyCookieSameSiteNoneWithCompatibleUserAgent(String userAgent) {
        val ctx = getCookieGenerationContext();
        ctx.setSameSitePolicy("none");

        val gen = new CookieRetrievingCookieGenerator(ctx);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        // Header value must not be null
        if (userAgent != null) {
            request.addHeader("User-Agent", userAgent);
        }

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(request, response, false, "CAS-Cookie-Value");
        val cookie = (MockCookie) response.getCookie(ctx.getName());
        assertNotNull(cookie);
        assertEquals("None", cookie.getSameSite());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // iOS 12 will treat None as Strict for all browsers
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0_0 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/12.0.0 Mobile/14A403 Safari/604.1.28",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0_1 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/12.0.1 Mobile/14A403 Safari/604.1.28",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_5 like Mac OS X) AppleWebKit/604.1.28 (KHTML, like Gecko) CriOS/60.0.0.0 Mobile/14E5239e Safari/602.1",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 12_4_5 like Mac OS X) AppleWebKit/600.1.4 (KHTML, like Gecko) FxiOS/1.0 Mobile/12F69 Safari/600.1.4",
            // MacOS 10.14 will treat None as Strict
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14) AppleWebKit/604.1.28 (KHTML, like Gecko) Version/10.14.0 Safari/604.1.28",
            // Chrome version >= 51 <=66 will reject cookie with SameSite=None
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/51.0.0.0 Safari/537.36.0",
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/52.0.8.15 Safari/537.36.0",
            "Mozilla/5.0 (Windows NT 6.4) AppleWebKit/537.36.0 (KHTML, like Gecko) Chrome/66.0.0.0 Safari/537.36.0",
            // Android webview uses Chrome an will also reject cookie with SameSite=None for chrome version >= 51 <=66
            "Mozilla/5.0 (Linux; Android 6.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/51.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/60.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Linux; Android 6.0.0; Pixel Build/Unknown; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/66.0.0.0 Mobile Safari/537.36",
    })
    public void verifyCookieSameSiteNoneWithIncompatibleUserAgent(String userAgent) {
        val ctx = getCookieGenerationContext();
        ctx.setSameSitePolicy("none");

        val gen = new CookieRetrievingCookieGenerator(ctx);
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.addHeader("User-Agent", userAgent);

        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        gen.addCookie(request, response, false, "CAS-Cookie-Value");
        val cookie = (MockCookie) response.getCookie(ctx.getName());
        assertNotNull(cookie);
        assertNull(cookie.getSameSite());
    }
}
