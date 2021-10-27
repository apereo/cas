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
package org.jasig.cas.web.init;

import javax.servlet.ServletContextEvent;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

/**
 * Test case for SafeContextLoaderListener.
 *
 * @author Andrew Petro
 * @since 3.0
 */
public class SafeContextLoaderListenerTests {

    private MockServletContext servletContext;

    private ServletContextEvent servletContextEvent;

    private SafeContextLoaderListener listener;

    @Before
    public void setUp() throws Exception {
        this.listener = new SafeContextLoaderListener();
        this.servletContext = new MockServletContext();
        this.servletContextEvent = new ServletContextEvent(this.servletContext);
    }

    /**
     * Test that SafeContextLoaderListener does not propagate exceptions thrown
     * by its delegate in contextInitialized().
     */
    @Test
    public void testContextInitialized() {
        /*
         * this testcase relies upon the fact that ContextLoaderListener()
         * throws a FileNotFound exception when invoked in the context of this
         * testcase because it does not find the resource
         * /WEB-INF/applicationContext.xml if our SafeContextLoaderListener
         * instance also throws the exception its delegate threw, this testcase
         * will fail. If it catches the exception, this test method will return
         * without having failed and so indicate success.
         */

        this.listener.contextInitialized(this.servletContextEvent);
    }

    @Test
    public void testContextDestroy() {
        this.listener.contextDestroyed(this.servletContextEvent);
    }
}
