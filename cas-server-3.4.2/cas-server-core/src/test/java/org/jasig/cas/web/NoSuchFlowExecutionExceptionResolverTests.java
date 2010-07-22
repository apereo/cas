/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class NoSuchFlowExecutionExceptionResolverTests extends TestCase {

    private NoSuchFlowExecutionExceptionResolver resolver;

    protected void setUp() throws Exception {
        this.resolver = new NoSuchFlowExecutionExceptionResolver();
    }

    public void testNullPointerException() {
        assertNull(this.resolver.resolveException(new MockHttpServletRequest(),
            new MockHttpServletResponse(), null, new NullPointerException()));
    }

    public void testNoSuchFlowExecutionException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("test");
        ModelAndView model = (this.resolver.resolveException(request,
            new MockHttpServletResponse(), null,
            new NoSuchFlowExecutionException(new FlowExecutionKey(){
            
                private static final long serialVersionUID = 1443616250214416520L;

                public String toString() {
                    return "test";
                }

                @Override
                public boolean equals(Object o) {
                    return true;
                }

                @Override
                public int hashCode() {
                    return 0;
                }
            }, new RuntimeException())));

        assertEquals(request.getRequestURI(), ((RedirectView) model.getView())
            .getUrl());
    }
    
    public void testNoSuchFlowExecutionExeptionWithQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("test");
        request.setQueryString("test=test");
        ModelAndView model = (this.resolver.resolveException(request,
            new MockHttpServletResponse(), null,
            new NoSuchFlowExecutionException(new FlowExecutionKey(){
                
                private static final long serialVersionUID = -4750073902540974152L;

                public String toString() {
                    return "test";
                }

                @Override
                public boolean equals(Object o) {
                    return true;
                }

                @Override
                public int hashCode() {
                    return 0;
                }
            }, new RuntimeException())));

        assertEquals(request.getRequestURI() + "?" + request.getQueryString(), ((RedirectView) model.getView())
            .getUrl());
    }

}
