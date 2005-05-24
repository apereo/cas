/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.flow.execution.NoSuchFlowExecutionException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

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
        this.resolver.afterPropertiesSet();
    }

    public void testNullPointerException() {
        assertNull(this.resolver.resolveException(new MockHttpServletRequest(),
            new MockHttpServletResponse(), null, new NullPointerException()));
    }

    public void testNoSuchFlowExecutionException() {
        this.resolver.setLoginUrl("test");
        this.resolver.afterPropertiesSet();
        ModelAndView model = (this.resolver.resolveException(new MockHttpServletRequest(),
            new MockHttpServletResponse(), null,
            new NoSuchFlowExecutionException("test")));
        
        assertEquals("test", ((RedirectView) model.getView()).getUrl());
    }

}
