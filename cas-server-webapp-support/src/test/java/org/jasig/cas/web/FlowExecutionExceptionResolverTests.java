/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import static org.junit.Assert.*;


import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.FlowExecutionKey;
import org.springframework.webflow.execution.repository.BadlyFormattedFlowExecutionKeyException;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class FlowExecutionExceptionResolverTests {

    private FlowExecutionExceptionResolver resolver;

    @Before
    public void setUp() throws Exception {
        this.resolver = new FlowExecutionExceptionResolver();
    }

    @Test
    public void verifyNullPointerException() {
        assertNull(this.resolver.resolveException(new MockHttpServletRequest(),
                new MockHttpServletResponse(), null, new NullPointerException()));
    }

    @Test
    public void verifyNoSuchFlowExecutionException() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("test");
        ModelAndView model = this.resolver.resolveException(request,
                new MockHttpServletResponse(), null,
                new NoSuchFlowExecutionException(new FlowExecutionKey(){

                    private static final long serialVersionUID = 1443616250214416520L;

                    @Override
                    public String toString() {
                        return "test";
                    }

                    @Override
                    public boolean equals(final Object o) {
                        return true;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                }, new RuntimeException()));

        assertEquals(request.getRequestURI(), ((RedirectView) model.getView())
                .getUrl());
    }

    @Test
    public void verifyBadlyFormattedExecutionException() {
        assertNull(this.resolver.resolveException(new MockHttpServletRequest(),
                new MockHttpServletResponse(), null,
                new BadlyFormattedFlowExecutionKeyException("invalidKey", "e2s1")));
    }

    @Test
    public void verifyNoSuchFlowExecutionExeptionWithQueryString() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("test");
        request.setQueryString("test=test");
        ModelAndView model = this.resolver.resolveException(request,
                new MockHttpServletResponse(), null,
                new NoSuchFlowExecutionException(new FlowExecutionKey(){

                    private static final long serialVersionUID = -4750073902540974152L;

                    @Override
                    public String toString() {
                        return "test";
                    }

                    @Override
                    public boolean equals(final Object o) {
                        return true;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }
                }, new RuntimeException()));

        assertEquals(request.getRequestURI() + "?" + request.getQueryString(), ((RedirectView) model.getView())
                .getUrl());
    }
}
