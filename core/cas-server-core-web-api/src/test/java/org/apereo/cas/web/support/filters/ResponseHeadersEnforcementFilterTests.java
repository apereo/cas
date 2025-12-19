package org.apereo.cas.web.support.filters;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.conversation.NoSuchConversationException;
import org.springframework.webflow.conversation.impl.SimpleConversationId;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ResponseHeadersEnforcementFilterTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Web")
class ResponseHeadersEnforcementFilterTests {
    private ResponseHeadersEnforcementFilter filter;

    private MockFilterConfig filterConfig;

    @BeforeEach
    void setup() {
        val servletContext = new MockServletContext();
        this.filterConfig = new MockFilterConfig(servletContext);
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_CACHE_CONTROL, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_TRANSPORT_SECURITY, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_STRICT_XFRAME_OPTIONS, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XCONTENT_OPTIONS, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_ENABLE_XSS_PROTECTION, "true");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY, "default-src https");
        filterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CACHE_CONTROL_STATIC_RESOURCES, "css|js|png|txt|jpg|ico|jpeg|bmp|gif");
        this.filter = new ResponseHeadersEnforcementFilter();
    }

    @Test
    void verifyUnrecognizedParam() {
        filterConfig.addInitParameter("bad-param", "bad-value");
        assertThrows(RuntimeException.class, () -> filter.init(filterConfig));
    }

    @Test
    void verifyMissingFlowConversation() {
        val mockFilter = new ResponseHeadersEnforcementFilter() {
            @Override
            protected Optional<RegisteredService> prepareFilterBeforeExecution(final HttpServletResponse httpServletResponse, final HttpServletRequest httpServletRequest) {
                throw new RuntimeException(new IllegalArgumentException(new NoSuchConversationException(new SimpleConversationId(UUID.randomUUID().toString()))));
            }
        };
        val servletRequest = new MockHttpServletRequest();
        val servletResponse = new MockHttpServletResponse();
        assertThrows(NoSuchConversationException.class, () -> mockFilter.doFilter(servletRequest, servletResponse, new MockFilterChain()));
        assertNotNull(servletRequest.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
    }

    @Test
    void verifyParam() {
        filter.init(filterConfig);

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        val servletResponse = new MockHttpServletResponse();
        assertThrows(RuntimeException.class, () -> filter.doFilter(servletRequest, servletResponse, null));
        assertDoesNotThrow(() -> filter.doFilter(servletRequest, servletResponse, new MockFilterChain()));
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

    @Test
    void verifyNoCacheParamJpeg() {
        filter.init(filterConfig);

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        servletRequest.setRequestURI("test.jpeg");
        val servletResponse = new MockHttpServletResponse();
        assertThrows(RuntimeException.class, () -> filter.doFilter(servletRequest, servletResponse, null));
        assertDoesNotThrow(() -> filter.doFilter(servletRequest, servletResponse, new MockFilterChain()));
        filter.destroy();
        assertNull(servletResponse.getHeaderValue("Cache-Control"));
        assertNull(servletResponse.getHeaderValue("Pragma"));
    }

    @Test
    void verifyNoCacheParamPng() {
        filter.init(filterConfig);

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        servletRequest.setRequestURI("test.png");
        val servletResponse = new MockHttpServletResponse();
        assertThrows(RuntimeException.class, () -> filter.doFilter(servletRequest, servletResponse, null));
        assertDoesNotThrow(() -> filter.doFilter(servletRequest, servletResponse, new MockFilterChain()));
        filter.destroy();
        assertNull(servletResponse.getHeaderValue("Cache-Control"));
        assertNull(servletResponse.getHeaderValue("Pragma"));
    }

    @Test
    void verifyDynamicNonce() {
        val servletContext = new MockServletContext();
        val specificFilterConfig = new MockFilterConfig(servletContext);
        specificFilterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY, "script-src '@nonce@';");
        filter.init(specificFilterConfig);

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        val servletResponse = new MockHttpServletResponse();
        filter.doFilter(servletRequest, servletResponse, new MockFilterChain());
        filter.destroy();

        val generatedNonce = servletRequest.getAttribute(ResponseHeadersEnforcementFilter.CSP_GENERATED_NONCE);
        assertNotNull(generatedNonce);
        val contentSecurityPolicy = servletResponse.getHeaderValue("Content-Security-Policy");
        assertEquals("script-src '" + generatedNonce + "';", contentSecurityPolicy);
    }

    @Test
    void verifyDynamicNonceAlreadyGenerated() {
        val servletContext = new MockServletContext();
        val specificFilterConfig = new MockFilterConfig(servletContext);
        specificFilterConfig.addInitParameter(ResponseHeadersEnforcementFilter.INIT_PARAM_CONTENT_SECURITY_POLICY, "script-src '@nonce@';");
        filter.init(specificFilterConfig);

        val servletRequest = new MockHttpServletRequest();
        servletRequest.setSecure(true);
        val alreadyGeneratedNonce = "123456";
        servletRequest.setAttribute(ResponseHeadersEnforcementFilter.CSP_GENERATED_NONCE, alreadyGeneratedNonce);
        val servletResponse = new MockHttpServletResponse();
        filter.doFilter(servletRequest, servletResponse, new MockFilterChain());
        filter.destroy();

        val generatedNonce = servletRequest.getAttribute(ResponseHeadersEnforcementFilter.CSP_GENERATED_NONCE);
        assertEquals(alreadyGeneratedNonce, generatedNonce);
        val contentSecurityPolicy = servletResponse.getHeaderValue("Content-Security-Policy");
        assertEquals("script-src '" + alreadyGeneratedNonce + "';", contentSecurityPolicy);
    }
}
