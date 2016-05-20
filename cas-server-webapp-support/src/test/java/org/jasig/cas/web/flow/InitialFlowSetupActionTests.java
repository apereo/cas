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
package org.jasig.cas.web.flow;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Scott Battaglia
 * @since 3.0.0.5
 *
 */
public class InitialFlowSetupActionTests {
    private static final String CONST_CONTEXT_PATH = "/test";
    private static final String CONST_CONTEXT_PATH_2 = "/test1";

    private final InitialFlowSetupAction action = new InitialFlowSetupAction();

    private CookieRetrievingCookieGenerator warnCookieGenerator;

    private CookieRetrievingCookieGenerator tgtCookieGenerator;

    private ServicesManager servicesManager;

    @Before
    public void setUp() throws Exception {
        this.warnCookieGenerator = new CookieRetrievingCookieGenerator();
        this.warnCookieGenerator.setCookiePath("");
        this.tgtCookieGenerator = new CookieRetrievingCookieGenerator();
        this.tgtCookieGenerator.setCookiePath("");
        this.action.setTicketGrantingTicketCookieGenerator(this.tgtCookieGenerator);
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
        final ArgumentExtractor[] argExtractors = new ArgumentExtractor[] {new CasArgumentExtractor()};
        this.action.setArgumentExtractors(Arrays.asList(argExtractors));

        this.servicesManager = mock(ServicesManager.class);
        when(this.servicesManager.findServiceBy(any(Service.class))).thenReturn(TestUtils.getRegisteredService("test"));
        this.action.setServicesManager(this.servicesManager);

        this.action.afterPropertiesSet();
    }

    @Test
    public void verifySettingContextPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.doExecute(context);

        assertEquals(CONST_CONTEXT_PATH + "/", this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + "/", this.tgtCookieGenerator.getCookiePath());
    }

    @Test
    public void verifyResettingContexPath() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath(CONST_CONTEXT_PATH);
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.doExecute(context);

        assertEquals(CONST_CONTEXT_PATH + "/", this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + "/", this.tgtCookieGenerator.getCookiePath());

        request.setContextPath(CONST_CONTEXT_PATH_2);
        this.action.doExecute(context);

        assertNotSame(CONST_CONTEXT_PATH_2 + "/", this.warnCookieGenerator.getCookiePath());
        assertNotSame(CONST_CONTEXT_PATH_2 + "/", this.tgtCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + "/", this.warnCookieGenerator.getCookiePath());
        assertEquals(CONST_CONTEXT_PATH + "/", this.tgtCookieGenerator.getCookiePath());
    }

    @Test
    public void verifyNoServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));

        final Event event = this.action.execute(context);

        assertNull(WebUtils.getService(context));

        assertEquals("success", event.getId());
    }

    @Test
    public void verifyServiceFound() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "test");
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        final Event event = this.action.execute(context);

        assertEquals("test", WebUtils.getService(context).getId());
        assertNotNull(WebUtils.getRegisteredService(context));
        assertEquals("success", event.getId());
    }

    @Test(expected = NoSuchFlowExecutionException.class)
    public void disableFlowIfNoService() throws Exception {
        this.action.setEnableFlowOnAbsentServiceRequest(false);
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        this.action.execute(context);
    }
}
