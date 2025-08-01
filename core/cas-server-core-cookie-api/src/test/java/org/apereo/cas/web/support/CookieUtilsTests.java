package org.apereo.cas.web.support;

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
