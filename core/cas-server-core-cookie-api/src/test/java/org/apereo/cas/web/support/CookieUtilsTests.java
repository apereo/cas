package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.web.cookie.CookieGenerationContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CookieUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Cookie")
class CookieUtilsTests {
    @Test
    void verifyCookieMaxAge() {
        assertEquals(-1, CookieUtils.getCookieMaxAge("-1"));
        assertEquals(10, CookieUtils.getCookieMaxAge("10"));
        assertEquals(60, CookieUtils.getCookieMaxAge("PT1M"));
        assertEquals(Integer.MAX_VALUE, CookieUtils.getCookieMaxAge("99999999999"));
    }

    @Test
    void verifyCookieHeader() {
        val results = CookieUtils.createSetCookieHeader("value",
            new CookieProperties().withName("testCookie").withMaxAge("1"));
        assertFalse(results.isEmpty());
    }

    @Test
    void verifyCookieHeaderWithConfigContext() {
        val results = CookieUtils.createSetCookieHeader("value",
            CookieGenerationContext.builder().name("testCookie").maxAge(100).build());
        assertFalse(results.isEmpty());
    }

}
