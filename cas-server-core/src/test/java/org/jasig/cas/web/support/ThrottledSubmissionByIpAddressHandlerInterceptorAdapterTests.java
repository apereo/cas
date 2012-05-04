/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.test.MockRequestContext;
import org.springframework.webflow.execution.Event;

import junit.framework.TestCase;


public class ThrottledSubmissionByIpAddressHandlerInterceptorAdapterTests extends TestCase {

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
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
        MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
        this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), null);
        
        assert 1 == this.adapter.findCount(request, null, 60);
        assertTrue(this.adapter.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
    
    public void testSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRemoteAddr("111.111.111.111");
        MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "success"));
        request.setAttribute("flowRequestContext", context);
        
        this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), null);
        
        assert 0 == this.adapter.findCount(request, null, 60);
        assertTrue(this.adapter.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
    
    public void testEnoughFailuresToCauseProblem() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
        MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), null);
       }

        assertFalse(this.adapter.preHandle(request,new MockHttpServletResponse(), new Object()));
    }
    
    public void testFailuresThenSuccess() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRemoteAddr("111.111.111.111");
        MockRequestContext context = new MockRequestContext();
        context.setCurrentEvent(new Event("", "error"));
        request.setAttribute("flowRequestContext", context);
       for (int i = 0; i < CONST_FAILURE_THRESHHOLD+1; i++) {
           this.adapter.postHandle(request, new MockHttpServletResponse(), new Object(), null);
       }
        
       assertFalse(this.adapter.preHandle(request,new MockHttpServletResponse(), new Object()));

       for (int i = 0; i < CONST_FAILURE_THRESHHOLD; i++) {
           this.adapter.decrementCounts();
       }
       
       assertTrue(this.adapter.preHandle(request, new MockHttpServletResponse(), new Object()));
    }
}
