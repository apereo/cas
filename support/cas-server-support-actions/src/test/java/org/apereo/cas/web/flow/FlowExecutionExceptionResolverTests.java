package org.apereo.cas.web.flow;

import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.cas.web.flow.executor.ClientFlowExecutionRepositoryException;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FlowExecutionExceptionResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Webflow")
public class FlowExecutionExceptionResolverTests {
    @Test
    public void verifyActionNull() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val r = new FlowExecutionExceptionResolver();
        assertNull(r.resolveException(request, response, new Object(), new RuntimeException()));
    }

    @Test
    public void verifyActionModelView() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        request.setQueryString("param=value&something=something");
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val r = new FlowExecutionExceptionResolver();
        val mv = r.resolveException(request, response, new Object(), new ClientFlowExecutionRepositoryException("error"));
        assertNotNull(mv);
        assertTrue(mv.getModel().containsKey(r.getModelKey()));
    }
}
