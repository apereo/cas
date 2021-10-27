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

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContextException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;


/**
 * Testcase for SafeDispatcherServlet.
 *
 * @author Andrew Petro
 * @since 3.0
 */
public class SafeDispatcherServletTests {

    private SafeDispatcherServlet safeServlet;

    private ServletContext mockContext;

    private MockServletConfig mockConfig;

    @Before
    public void setUp() throws Exception {

        this.safeServlet = new SafeDispatcherServlet();

        this.mockContext = new MockServletContext();
        this.mockConfig = new MockServletConfig(this.mockContext);
    }

    /*
     * Test that SafeDispatcherServlet does not propogate exceptions generated
     * by its underlying DispatcherServlet on init() and that it stores the
     * exception into the ServletContext as the expected attribute name.
     */
    @Test
    public void testInitServletConfig() {

        /*
         * we fail if safeServlet propogates exception we rely on the underlying
         * DispatcherServlet throwing an exception when init'ed in this way
         * without the servlet name having been set and without there being a
         * -servlet.xml that it can find on the classpath.
         */
        this.safeServlet.init(this.mockConfig);

        /*
         * here we test that the particular exception stored by the underlying
         * DispatcherServlet has been stored into the ServetContext as an
         * attribute as advertised by SafeDispatcherServlet. we rely on knowing
         * the particular exception that the underlying DispatcherServlet throws
         * under these circumstances;
         */
        BeanDefinitionStoreException bdse = (BeanDefinitionStoreException) this.mockContext
            .getAttribute(SafeDispatcherServlet.CAUGHT_THROWABLE_KEY);
        assertNotNull(bdse);

    }

    /*
     * Test that the SafeDispatcherServlet does not service requests when it has
     * failed init and instead throws an ApplicationContextException.
     */
    @Test
    public void testService() throws ServletException, IOException {
        this.safeServlet.init(this.mockConfig);

        ServletRequest mockRequest = new MockHttpServletRequest();
        ServletResponse mockResponse = new MockHttpServletResponse();

        try {
            this.safeServlet.service(mockRequest, mockResponse);
        } catch (final ApplicationContextException ace) {
            // good, threw the exception we expected.
            return;
        }

        fail("Should have thrown ApplicationContextException since init() failed.");
    }

    @Test
    public void testServiceSucceeds() {
        this.mockConfig = new MockServletConfig(this.mockContext, "cas");
        this.safeServlet.init(this.mockConfig);

        ServletRequest mockRequest = new MockHttpServletRequest();
        ServletResponse mockResponse = new MockHttpServletResponse();

        try {
            this.safeServlet.service(mockRequest, mockResponse);
        } catch (final ApplicationContextException e) {
            fail("Unexpected exception.");
        } catch (final Exception e) {
            return;
        }
    }
}
