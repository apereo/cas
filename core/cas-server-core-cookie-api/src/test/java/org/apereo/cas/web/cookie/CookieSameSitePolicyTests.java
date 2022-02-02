package org.apereo.cas.web.cookie;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        val opt = CookieSameSitePolicy.of(CookieGenerationContext.builder().sameSitePolicy("Off").build());
        assertNotNull(opt);
        assertTrue(opt.build(new MockHttpServletRequest(), new MockHttpServletResponse()).isEmpty());
    }


    @Test
    public void verifyNone() {
        val opt = CookieSameSitePolicy.of(CookieGenerationContext.builder().sameSitePolicy("NONE").build());
        assertNotNull(opt);
        assertEquals("SameSite=None;", opt.build(new MockHttpServletRequest(), new MockHttpServletResponse()).get());
    }

    @Test
    public void verifyLax() {
        val opt = CookieSameSitePolicy.of(CookieGenerationContext.builder().sameSitePolicy("LAX").build());
        assertNotNull(opt);
        assertEquals("SameSite=Lax;", opt.build(new MockHttpServletRequest(), new MockHttpServletResponse()).get());
    }

    @Test
    public void verifyStrict() {
        val opt = CookieSameSitePolicy.of(CookieGenerationContext.builder().sameSitePolicy("STRICT").build());
        assertNotNull(opt);
        assertEquals("SameSite=Strict;", opt.build(new MockHttpServletRequest(), new MockHttpServletResponse()).get());
    }

    @Test
    public void verifyCustomImpl() {
        val opt = CookieSameSitePolicy.of(CookieGenerationContext.builder()
            .sameSitePolicy(CustomCookieSameSitePolicy.class.getName()).build());
        assertNotNull(opt);
        assertEquals("SameSite=Something;", opt.build(new MockHttpServletRequest(), new MockHttpServletResponse()).get());
    }

    public static class CustomCookieSameSitePolicy implements CookieSameSitePolicy {
        @Override
        public Optional<String> build(final HttpServletRequest request, final HttpServletResponse response) {
            return Optional.of("SameSite=Something;");
        }
    }
}
