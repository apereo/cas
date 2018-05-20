package org.apereo.cas.web.flow;

import org.apereo.cas.web.FlowExecutionExceptionResolver;
import org.apereo.spring.webflow.plugin.ClientFlowExecutionRepositoryException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;
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
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final FlowExecutionExceptionResolver r = new FlowExecutionExceptionResolver();
        assertNull(r.resolveException(request, response, new Object(), new RuntimeException()));
    }

    @Test
    public void verifyActionModelView() {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/cas/login");
        request.setQueryString("param=value&something=something");
        final MockHttpServletResponse response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        final FlowExecutionExceptionResolver r = new FlowExecutionExceptionResolver();
        final ModelAndView mv = r.resolveException(request, response, new Object(), new ClientFlowExecutionRepositoryException("error"));
        assertNotNull(mv);
        assertTrue(mv.getModel().containsKey(r.getModelKey()));
    }
}
