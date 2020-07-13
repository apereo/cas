package org.apereo.cas.web.support.filters;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class ResponseHeadersEnforcementFilterTests {
    private ResponseHeadersEnforcementFilter filter;

    private MockFilterConfig filterConfig;

    @BeforeEach
    public void setup() {
        val servletContext = new MockServletContext();
        this.filterConfig = new MockFilterConfig(servletContext);
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.THROW_ON_ERROR, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_CACHE_CONTROL, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_TRANSPORT_SECURITY, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_XFRAME_OPTIONS, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XCONTENT_OPTIONS, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XSS_PROTECTION, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY, "default-src https");
        this.filter = new ResponseHeadersEnforcementFilter();
    }

    @Test
    public void verifyUnrecognizedParam() {
        filterConfig.addInitParameter("bad-param", "bad-value");
        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
    }

    @Test
    public void verifyParam() {
        filter.init(filterConfig);
        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        val servletResponse = new MockHttpServletResponse();
        assertDoesNotThrow(() -> {
            filter.doFilter(servletRequest, servletResponse, new MockFilterChain());
        });
        filter.destroy();
        assertNotNull(servletResponse.getHeaderValue("Cache-Control"));
        assertNotNull(servletResponse.getHeaderValue("Pragma"));
        assertNotNull(servletResponse.getHeaderValue("Expires"));
        assertNotNull(servletResponse.getHeaderValue("Content-Security-Policy"));
        assertNotNull(servletResponse.getHeaderValue("X-XSS-Protection"));
        assertNotNull(servletResponse.getHeaderValue("X-Frame-Options"));
        assertNotNull(servletResponse.getHeaderValue("X-Content-Type-Options"));
        assertNotNull(servletResponse.getHeaderValue("Strict-Transport-Security"));
    }
}
