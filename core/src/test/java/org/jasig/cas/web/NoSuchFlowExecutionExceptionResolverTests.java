/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.repository.conversation.NoSuchConversationException;
import org.springframework.webflow.execution.repository.conversation.impl.SimpleConversationId;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class NoSuchFlowExecutionExceptionResolverTests extends TestCase {

    private NoSuchConversationExceptionResolver resolver;

    protected void setUp() throws Exception {
        this.resolver = new NoSuchConversationExceptionResolver();
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
            new NoSuchConversationException(new SimpleConversationId("test"))));

        assertEquals(request.getRequestURI(), ((RedirectView) model.getView())
            .getUrl());
    }
    
    public void testNoSuchFlowExecutionExeptionWithQueryString() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("test");
        request.setQueryString("test=test");
        ModelAndView model = (this.resolver.resolveException(request,
            new MockHttpServletResponse(), null,
            new NoSuchConversationException(new SimpleConversationId("test"))));

        assertEquals(request.getRequestURI() + "?" + request.getQueryString(), ((RedirectView) model.getView())
            .getUrl());
    }

}
