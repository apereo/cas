package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Auke van Leeuwen
 * @since 4.2.0
 */
@Tag("Webflow")
class CasDefaultFlowUrlHandlerTests {

    private final FlowUrlHandler urlHandler = new CasDefaultFlowUrlHandler();

    private final MockHttpServletRequest request = new MockHttpServletRequest();

    @Test
    void verifyFlowExecutionKeyInRequestBody() throws Throwable {
        setupRequest("/cas", "/app", "/foo");
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("execution=continue".getBytes(StandardCharsets.UTF_8));
        val executionKey = urlHandler.getFlowExecutionKey(request);
        assertEquals("continue", executionKey);
        assertEquals("continue", request.getAttribute(CasDefaultFlowUrlHandler.DEFAULT_FLOW_EXECUTION_KEY_PARAMETER));
    }

    @Test
    void verifyCreateFlowExecutionUrlWithSingleValuedAttributes() throws Throwable {
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
    void verifyFlowIdWithAnchorTag() throws Throwable {
        setupRequest("/cas", "/app", "/foo#this-exists-here");
        request.setParameter("bar", "baz");
        request.setParameter("qux", "quux");
        val flowId = urlHandler.getFlowId(request);
        assertEquals("foo", flowId);
    }

    @Test
    void verifyCreateFlowExecutionUrlWithMultiValuedAttributes() throws Throwable {
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
