package org.apereo.cas.web.cookie;

import org.apereo.cas.web.support.mgmr.DefaultCookieSameSitePolicy;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCookieSameSitePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Cookie")
class DefaultCookieSameSitePolicyTests {
    @Test
    void verifyOff() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("Off").build();
        assertTrue(getPolicyResult(opt).isEmpty());
    }

    @Test
    void verifyNone() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("NONE").build();
        assertEquals("SameSite=None;", getPolicyResult(opt).get());
    }

    @Test
    void verifyLax() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("LAX").build();
        assertEquals("SameSite=Lax;", getPolicyResult(opt).get());
    }

    @Test
    void verifyStrict() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("STRICT").build();
        assertEquals("SameSite=Strict;", getPolicyResult(opt).get());
    }

    @Test
    void verifyCustomImpl() {
        val opt = CookieGenerationContext.builder()
            .sameSitePolicy(CustomCookieSameSitePolicy.class.getName())
            .build();
        assertEquals("SameSite=Something;", getPolicyResult(opt).get());
    }

    @Test
    void verifyGroovyImpl() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("classpath:/SameSiteCookie.groovy").build();
        assertEquals("SameSite=Something;", getPolicyResult(opt).get());
    }


    private static Optional<String> getPolicyResult(final CookieGenerationContext context) {
        return DefaultCookieSameSitePolicy.INSTANCE.build(new MockHttpServletRequest(), new MockHttpServletResponse(), context);
    }

    public static class CustomCookieSameSitePolicy implements CookieSameSitePolicy {
        @Override
        public Optional<String> build(final HttpServletRequest request, final HttpServletResponse response, final CookieGenerationContext cookieGenerationContext) {
            return Optional.of("SameSite=Something;");
        }
    }
}
