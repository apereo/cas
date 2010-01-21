/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.mock.MockValidationSpecification;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTest {

    private static final String CONST_SUCCESS_VIEW = "casServiceSuccessView";
    
    private static final String CONST_FAILURE_VIEW = "casServiceFailureView";
    
    private ServiceValidateController serviceValidateController;

    @Before
    public void onSetUp() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController
            .setCentralAuthenticationService(getCentralAuthenticationService());
        final Cas20ProxyHandler proxyHandler = new Cas20ProxyHandler();
        proxyHandler.setHttpClient(new HttpClient());
        this.serviceValidateController.setProxyHandler(proxyHandler);
        this.serviceValidateController.setApplicationContext(context);
        this.serviceValidateController.setArgumentExtractor(new CasArgumentExtractor());
    }

    private HttpServletRequest getHttpServletRequest() throws TicketException {
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        getCentralAuthenticationService().grantServiceTicket(tId,
            TestUtils.getService());
        final String sId2 = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId2);
        request.addParameter("renew", "true");

        return request;
    }

    @Test
    public void testAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController
            .setValidationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setSuccessView(CONST_SUCCESS_VIEW);
        this.serviceValidateController.setFailureView(CONST_FAILURE_VIEW);
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
    }

    @Test
    public void testEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(
            new MockHttpServletRequest(), new MockHttpServletResponse())
            .getModel().get("code"));
    }

    @Test
    public void testValidServiceTicket() throws Exception {
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId);

        assertEquals(CONST_SUCCESS_VIEW,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketInvalidSpec() throws Exception {

        assertEquals(CONST_FAILURE_VIEW,
            this.serviceValidateController.handleRequestInternal(
                getHttpServletRequest(), new MockHttpServletResponse())
                .getViewName());
    }

    @Test
    public void testValidServiceTicketRuntimeExceptionWithSpec()
        throws Exception {
        this.serviceValidateController
            .setValidationSpecificationClass(MockValidationSpecification.class);

        try {
            assertEquals(CONST_FAILURE_VIEW,
                this.serviceValidateController.handleRequestInternal(
                    getHttpServletRequest(), new MockHttpServletResponse())
                    .getViewName());
            fail(TestUtils.CONST_EXCEPTION_EXPECTED);
        } catch (RuntimeException e) {
            // nothing to do here, exception is expected.
        }
    }

    @Test
    public void testInvalidServiceTicket() throws Exception {
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        getCentralAuthenticationService().destroyTicketGrantingTicket(tId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId);

        assertEquals(CONST_FAILURE_VIEW,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId);
        request
            .addParameter("pgtUrl", "https://www.acs.rutgers.edu");

        assertEquals(CONST_SUCCESS_VIEW,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    @Test
    public void testValidServiceTicketWithBadPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "http://www.acs.rutgers.edu");

        final ModelAndView modelAndView = this.serviceValidateController
            .handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(CONST_SUCCESS_VIEW, modelAndView
            .getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }

    @Test
    public void testValidServiceTicketWithInvalidPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        final String tId = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
        final String sId = getCentralAuthenticationService()
            .grantServiceTicket(tId, TestUtils.getService());

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", TestUtils.getService()
            .getId());
        request.addParameter("ticket", sId);
        request.addParameter("pgtUrl", "duh");

        final ModelAndView modelAndView = this.serviceValidateController
            .handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(CONST_SUCCESS_VIEW, modelAndView
            .getViewName());
        assertNull(modelAndView.getModel().get("pgtIou"));
    }
}
