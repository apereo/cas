package org.apereo.cas.web.flow;

import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.spring.webflow.plugin.ClientFlowExecutionRepositoryException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link FlowExecutionExceptionResolverTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class FlowExecutionExceptionResolverTests {
    @Test
    public void verifyActionNull() {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final var r = new FlowExecutionExceptionResolver();
        assertNull(r.resolveException(request, response, new Object(), new RuntimeException()));
    }

    @Test
    public void verifyActionModelView() {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        request.setQueryString("param=value&something=something");
        final var response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final var r = new FlowExecutionExceptionResolver();
        final var mv = r.resolveException(request, response, new Object(), new ClientFlowExecutionRepositoryException("error"));
        assertNotNull(mv);
        assertTrue(mv.getModel().containsKey(r.getModelKey()));
    }
}
