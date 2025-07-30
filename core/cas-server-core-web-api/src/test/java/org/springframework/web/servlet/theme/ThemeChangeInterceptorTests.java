package org.springframework.web.servlet.theme;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.DispatcherServlet;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ThemeChangeInterceptorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Web")
class ThemeChangeInterceptorTests {

    @Test
    void verifyOperation() throws Exception {
        val interceptor = new ThemeChangeInterceptor();
        val request = new MockHttpServletRequest();
        val themeResolver = new SessionThemeResolver();
        request.setAttribute(DispatcherServlet.THEME_RESOLVER_ATTRIBUTE, themeResolver);
        request.setParameter(ThemeChangeInterceptor.DEFAULT_PARAM_NAME, "cas");
        assertTrue(interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));
        assertEquals("cas", themeResolver.resolveThemeName(request));
    }
}
