package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasDefaultFlowUrlHandler;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.context.servlet.FlowUrlHandler;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Auke van Leeuwen
 * @since 4.2.0
 */
@Tag("Webflow")
class CasDefaultFlowUrlHandlerTests extends BaseWebflowConfigurerTests {

    @Autowired
    @Qualifier("loginFlowUrlHandler")
    private FlowUrlHandler loginFlowUrlHandler;

    @Test
    void verifyFlowExecutionKeyInRequestBody() {
        val request = setupRequest("/cas", "/app", "/foo");
        request.setMethod("POST");
        request.setContentType("application/x-www-form-urlencoded");
        request.setContent("execution=continue".getBytes(StandardCharsets.UTF_8));
        val executionKey = loginFlowUrlHandler.getFlowExecutionKey(request);
        assertEquals("continue", executionKey);
        assertEquals("continue", request.getAttribute(CasDefaultFlowUrlHandler.DEFAULT_FLOW_EXECUTION_KEY_PARAMETER));
    }

    @Test
    void verifyCreateFlowExecutionUrlWithSingleValuedAttributes() {
        val request = setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", "baz");
        request.setParameter("qux", "quux");
        val url = loginFlowUrlHandler.createFlowExecutionUrl("foo", "12345", request);
        assertEquals("/cas/app/foo?bar=baz&qux=quux&execution=12345", url);
        request.addParameter(CasDefaultFlowUrlHandler.DEFAULT_FLOW_EXECUTION_KEY_PARAMETER, "12345");
        assertNotNull(loginFlowUrlHandler.getFlowExecutionKey(request));
        assertNotNull(loginFlowUrlHandler.createFlowDefinitionUrl("cas", new LocalAttributeMap<>(), request));
    }

    @Test
    void verifyFlowIdWithAnchorTag() {
        val request = setupRequest("/cas", "/app", "/foo#this-exists-here");
        request.setParameter("bar", "baz");
        request.setParameter("qux", "quux");
        val flowId = loginFlowUrlHandler.getFlowId(request);
        assertEquals("foo", flowId);
    }

    @Test
    void verifyCreateFlowExecutionUrlWithMultiValuedAttributes() {
        val request = setupRequest("/cas", "/app", "/foo");
        request.setParameter("bar", "baz1", "baz2");
        request.setParameter("qux", "quux");
        val url = loginFlowUrlHandler.createFlowExecutionUrl("foo", "12345", request);

        assertEquals("/cas/app/foo?bar=baz1&bar=baz2&qux=quux&execution=12345", url);
    }

    private static MockHttpServletRequest setupRequest(final String contextPath,
                                                       final String servletPath,
                                                       final String pathInfo) {
        val request = new MockHttpServletRequest();
        request.setContextPath(contextPath);
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        request.setRequestURI(contextPath + servletPath + pathInfo);
        return request;
    }
}
