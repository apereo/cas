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
