/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.math.BigInteger;

import org.jasig.cas.web.support.ThrottledSubmissionByIpAddressHandlerInterceptorAdapter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;


public class ThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests extends
    TestCase {

    private ThrottledSubmissionByIpAddressHandlerInterceptorAdapter adapter;
    
    private static final int CONST_FAILURE_THRESHHOLD = 3;
    
    private static final int CONST_FAILURE_TIMEOUT = 2;

    protected void setUp() throws Exception {
        this.adapter = new ThrottledSubmissionByIpAddressHandlerInterceptorAdapter();
        this.adapter.setFailureThreshhold(CONST_FAILURE_THRESHHOLD);
        this.adapter.setFailureTimeout(CONST_FAILURE_TIMEOUT);
        this.adapter.afterPropertiesSet();
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
    
    public void testEnoughFailuresToChangeView() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("casLoginView");
        request.setMethod("GET");
        request.setRemoteAddr("111.111.111.111");
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
       }
        
       assertEquals("casFailureAuthenticationThreshhold", modelAndView.getViewName());
    }
    
    public void testFailuresThenSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final ModelAndView modelAndView = new ModelAndView("casLoginView");
        request.setMethod("GET");
        request.setRemoteAddr("111.111.111.111");
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
       }
        
       assertEquals("casFailureAuthenticationThreshhold", modelAndView.getViewName());
       
       Thread.sleep((CONST_FAILURE_TIMEOUT + 10) * 1000);
     
       modelAndView.setViewName("casLoginView");
       
       this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), modelAndView);
       assertEquals("casLoginView", modelAndView.getViewName());
    }
}
