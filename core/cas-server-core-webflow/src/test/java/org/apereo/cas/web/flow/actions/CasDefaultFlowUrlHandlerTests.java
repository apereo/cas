package org.apereo.cas.web.flow.actions;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.*;

/**
 * @author Auke van Leeuwen
 * @since 4.2.0
 */
public class CasDefaultFlowUrlHandlerTests {

    private final CasDefaultFlowUrlHandler urlHandler = new CasDefaultFlowUrlHandler();

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    public void verifyCreateFlowExecutionUrlWithSingleValuedAttributes() {
        setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", "baz");
        request.setParameter("qux", "quux");
        final String url = urlHandler.createFlowExecutionUrl("foo", "12345", request);

        assertEquals("/cas/app/foo?bar=baz&qux=quux&execution=12345", url);
    }

    @Test
    public void verifyCreateFlowExecutionUrlWithMultiValuedAttributes() {
        setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", new String[]{"baz1", "baz2"});
        request.setParameter("qux", "quux");
        final String url = urlHandler.createFlowExecutionUrl("foo", "12345", request);

        assertEquals("/cas/app/foo?bar=baz1&bar=baz2&qux=quux&execution=12345", url);
    }

    private void setupRequest(final String contextPath, final String servletPath, final String pathInfo) {
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        request.setRequestURI(contextPath + servletPath + pathInfo);
    }
}
