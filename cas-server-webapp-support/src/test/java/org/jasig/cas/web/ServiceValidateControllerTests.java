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

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.mock.MockValidationSpecification;
import org.jasig.cas.ticket.proxy.ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.util.SimpleHttpClient;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Scott Battaglia
 * @since 3.0
 */
public class ServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTest {

    private ServiceValidateController serviceValidateController;

    @Before
    public void onSetUp() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController.setCentralAuthenticationService(getCentralAuthenticationService());
        final Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler();
        proxyHandler.setHttpClient(new SimpleHttpClient());
        this.serviceValidateController.setProxyHandler(proxyHandler);
        this.serviceValidateController.setApplicationContext(context);
        this.serviceValidateController.setArgumentExtractor(new CasArgumentExtractor());
    }

    private HttpServletRequest getHttpServletRequest() throws Exception {
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());
        final String sId2 = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId2);
        request.addParameter("renew", "true");

        return request;
    }

    @Test
    public void testAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
    }

    @Test
    public void testEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
                new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get("code"));
    }

    @Test
    public void testValidServiceTicket() throws Exception {
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketInvalidSpec() throws Exception {
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
    }

    @Test(expected=RuntimeException.class)
    public void testValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        this.serviceValidateController.setValidationSpecificationClass(MockValidationSpecification.class);

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(getHttpServletRequest(), new MockHttpServletResponse()).getViewName());
        fail(TestUtils.CONST_EXCEPTION_EXPECTED);
    }

    @Test
    public void testInvalidServiceTicket() throws Exception {
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithValidPgtNoProxyHandling() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
                .grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "https://www.github.com");

        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME,
                this.serviceValidateController.handleRequestInternal(request,
                        new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithInsecurePgtUrl() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "http://www.github.com");

        final ModelAndView modelAndView = this.serviceValidateController
                .handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        
    }

    @Test
    public void testValidServiceTicketWithInvalidPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "duh");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void testValidServiceTicketWithValidPgtAndProxyHandling() throws Exception {
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "https://www.github.com");

        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_SUCCESS_VIEW_NAME, modelAndView.getViewName());
        assertNotNull(modelAndView.getModel().get("pgtIou"));
    }
    
    @Test
    public void testValidServiceTicketWithValidPgtAndProxyHandlerFailing() throws Exception {
        final String tId = getCentralAuthenticationService()
                .createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService().grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService().getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "https://www.github.com");

        this.serviceValidateController.setProxyHandler(new ProxyHandler() {
            @Override
            public String handle(final Credential credential, final String proxyGrantingTicketId) {
                return null;
            }
            
            @Override
            public boolean canHandle(final Credential credential) {
                return true;
            }
        });
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ServiceValidateController.DEFAULT_SERVICE_FAILURE_VIEW_NAME, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
}
