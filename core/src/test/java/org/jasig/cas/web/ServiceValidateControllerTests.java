/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.mock.MockValidationSpecification;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class ServiceValidateControllerTests extends AbstractCentralAuthenticationServiceTest {

    private ServiceValidateController serviceValidateController;

    protected void setUp() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.serviceValidateController.setApplicationContext(context);

        this.serviceValidateController.afterPropertiesSet();
    }

    public void testAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController
            .setValidationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setFailureView("test");
        this.serviceValidateController.setSuccessView("test");
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
        this.serviceValidateController.afterPropertiesSet();
    }
    
    public void testEmptyParams() throws Exception {
        assertNotNull(this.serviceValidateController.handleRequestInternal(new MockHttpServletRequest(), new MockHttpServletResponse()).getModel().get(WebConstants.CODE));
    }

    public void testValidServiceTicket() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);

        assertEquals(ViewNames.CONST_SERVICE_SUCCESS,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    public void testValidServiceTicketInvalidSpec() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        this.centralAuthenticationService.grantServiceTicket(tId,
            new SimpleService("test"));
        final String sId2 = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId2);
        request.addParameter(WebConstants.RENEW, "true");

        assertEquals(ViewNames.CONST_SERVICE_FAILURE,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }
    
    public void testValidServiceTicketRuntimeExceptionWithSpec() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        this.serviceValidateController.setValidationSpecificationClass(MockValidationSpecification.class);
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        this.centralAuthenticationService.grantServiceTicket(tId,
            new SimpleService("test"));
        final String sId2 = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId2);
        request.addParameter(WebConstants.RENEW, "true");

        try {
        assertEquals(ViewNames.CONST_SERVICE_FAILURE,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
            fail("RuntimeException expected.");
        } catch (RuntimeException e) {
            return;
        }
    }

    public void testInvalidServiceTicket() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        this.centralAuthenticationService.destroyTicketGrantingTicket(tId);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);

        assertEquals(ViewNames.CONST_SERVICE_FAILURE,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    public void testValidServiceTicketWithPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        request
            .addParameter(WebConstants.PGTURL, "https://www.acs.rutgers.edu");

        assertEquals(ViewNames.CONST_SERVICE_SUCCESS,
            this.serviceValidateController.handleRequestInternal(request,
                new MockHttpServletResponse()).getViewName());
    }

    public void testValidServiceTicketWithBadPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUsername("test");
        final String tId = this.centralAuthenticationService
            .createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService
            .grantServiceTicket(tId, new SimpleService("test"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        request.addParameter(WebConstants.PGTURL, "http://www.acs.rutgers.edu");

        final ModelAndView modelAndView = this.serviceValidateController
            .handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ViewNames.CONST_SERVICE_SUCCESS, modelAndView
            .getViewName());
        assertNull(modelAndView.getModel().get(WebConstants.PGTIOU));
    }
}
