package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.core.collection.LocalAttributeMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Auke van Leeuwen
 * @since 4.2.0
 */
@Tag("Webflow")
public class CasDefaultFlowUrlHandlerTests {

    private final CasDefaultFlowUrlHandler urlHandler = new CasDefaultFlowUrlHandler();

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    public void verifyCreateFlowExecutionUrlWithSingleValuedAttributes() {
        setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", "baz");
        request.setParameter("qux", "quux");
        val url = urlHandler.createFlowExecutionUrl("foo", "12345", request);
        assertEquals("/cas/app/foo?bar=baz&qux=quux&execution=12345", url);
        request.addParameter(CasDefaultFlowUrlHandler.DEFAULT_FLOW_EXECUTION_KEY_PARAMETER, "12345");
        assertNotNull(urlHandler.getFlowExecutionKey(request));
        assertNotNull(urlHandler.createFlowDefinitionUrl("cas", new LocalAttributeMap<>(), request));
    }

    @Test
    public void verifyCreateFlowExecutionUrlWithMultiValuedAttributes() {
        setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", "baz1", "baz2");
        request.setParameter("qux", "quux");
        val url = urlHandler.createFlowExecutionUrl("foo", "12345", request);

        assertEquals("/cas/app/foo?bar=baz1&bar=baz2&qux=quux&execution=12345", url);
    }

    private void setupRequest(final String contextPath, final String servletPath, final String pathInfo) {
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        request.setRequestURI(contextPath + servletPath + pathInfo);
    }
}
