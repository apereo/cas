/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import org.jasig.cas.web.support.AbstractInMemoryThrottledSubmissionHandlerInterceptorAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;


public class ThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests extends
    TestCase {

    private InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter adapter;
    
    private static final int CONST_FAILURE_THRESHHOLD = 3;
    
    private static final int CONST_FAILURE_TIMEOUT = 2;

    protected void setUp() throws Exception {
        this.adapter = new InMemoryThrottledSubmissionByIpAddressHandlerInterceptorAdapter();
        this.adapter.setFailureThreshold(CONST_FAILURE_THRESHHOLD);
        this.adapter.setFailureRangeInSeconds(CONST_FAILURE_TIMEOUT);
    }
    
    public void testOneFailure() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("casLoginView");
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
        this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
        
        assertEquals("casLoginView", modelAndView.getViewName());
    }
    
    public void testSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("redirect");
        request.setMethod("GET");
        request.setRemoteAddr("111.111.111.111");
        
        this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
        
        assertEquals("redirect", modelAndView.getViewName());
    }
    
    public void testEnoughFailuresToCauseProblem() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("casLoginView");
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
       }

        assertFalse(this.adapter.preHandle(request,new MockHttpServletResponse(), new Object()));
    }
    
    public void testFailuresThenSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("casLoginView");
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
       }
        
       assertFalse(this.adapter.preHandle(request,new MockHttpServletResponse(), new Object()));

       for (int i = 0; i < CONST_FAILURE_THRESHHOLD; i++) {
           this.adapter.decrementCounts();
       }
       
       assertTrue(this.adapter.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
