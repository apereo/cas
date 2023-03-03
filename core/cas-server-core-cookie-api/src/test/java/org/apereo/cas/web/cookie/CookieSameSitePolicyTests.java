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
 * This is {@link CookieSameSitePolicyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Cookie")
public class CookieSameSitePolicyTests {
    @Test
    public void verifyOff() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("Off").build();
        assertNotNull(opt);
        assertTrue(getPolicyResult(opt).isEmpty());
    }

    @Test
    public void verifyNone() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("NONE").build();
        assertNotNull(opt);
        assertEquals("SameSite=None;", getPolicyResult(opt));
    }

    @Test
    public void verifyLax() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("LAX").build();
        assertNotNull(opt);
        assertEquals("SameSite=Lax;", getPolicyResult(opt));
    }

    @Test
    public void verifyStrict() {
        val opt = CookieGenerationContext.builder().sameSitePolicy("STRICT").build();
        assertNotNull(opt);
        assertEquals("SameSite=Strict;", getPolicyResult(opt));
    }

    @Test
    public void verifyCustomImpl() {
        val opt = CookieGenerationContext.builder()
            .sameSitePolicy(CustomCookieSameSitePolicy.class.getName()).build();
        assertNotNull(opt);
        assertEquals("SameSite=Something;", getPolicyResult(opt));
    }

    private static String getPolicyResult(final CookieGenerationContext context) {
        return DefaultCookieSameSitePolicy.INSTANCE.build(new MockHttpServletRequest(), new MockHttpServletResponse(), context).get();
    }

    public static class CustomCookieSameSitePolicy implements CookieSameSitePolicy {
        @Override
        public Optional<String> build(final HttpServletRequest request, final HttpServletResponse response, final CookieGenerationContext cookieGenerationContext) {
            return Optional.of("SameSite=Something;");
        }
    }
}
