/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import java.util.ArrayList;
import java.util.List;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.DefaultAuthenticationAttributesPopulator;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.proxy.support.Cas10ProxyHandler;
import org.jasig.cas.ticket.proxy.support.Cas20ProxyHandler;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.validation.Cas20ProtocolValidationSpecification;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ServiceValidateControllerTests extends TestCase {

    private ServiceValidateController serviceValidateController;
    private CentralAuthenticationServiceImpl centralAuthenticationService;

    private CentralAuthenticationService getCentralAuthenticationService() {
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl();
        this.centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService.setTicketRegistry(new DefaultTicketRegistry());
        this.centralAuthenticationService.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        
        List populators = new ArrayList();
        populators.add(new DefaultAuthenticationAttributesPopulator());
        
        List resolvers = new ArrayList();
        resolvers.add(new DefaultCredentialsToPrincipalResolver());
        resolvers.add(new HttpBasedServiceCredentialsToPrincipalResolver());
        
        List handlers = new ArrayList();
        handlers.add(new SimpleTestUsernamePasswordAuthenticationHandler());
        handlers.add(new HttpBasedServiceCredentialsAuthenticationHandler());
        
        manager.setAuthenticationAttributesPopulators(populators);
        manager.setAuthenticationHandlers(handlers);
        manager.setCredentialsToPrincipalResolvers(resolvers);
        
        this.centralAuthenticationService.setAuthenticationManager(manager);
        
        return this.centralAuthenticationService;
    }
    
    
    protected void setUp() throws Exception {
        StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.serviceValidateController = new ServiceValidateController();
        this.serviceValidateController.setCentralAuthenticationService(getCentralAuthenticationService());
        this.serviceValidateController.setApplicationContext(context);
        
        this.serviceValidateController.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoAuthenticationService() {
        this.serviceValidateController.setCentralAuthenticationService(null);
        try {
            this.serviceValidateController.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testAfterPropertesSetTestEverything() throws Exception {
        this.serviceValidateController.setAuthenticationSpecificationClass(Cas20ProtocolValidationSpecification.class);
        this.serviceValidateController.setFailureView("test");
        this.serviceValidateController.setSuccessView("test");
        this.serviceValidateController.setProxyHandler(new Cas20ProxyHandler());
        this.serviceValidateController.afterPropertiesSet();
    }
    
    public void testValidServiceTicket() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUserName("test");
        final String tId = this.centralAuthenticationService.createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        
        assertEquals(ViewNames.CONST_SERVICE_SUCCESS, this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getViewName());
    }

    public void testValidServiceTicketInvalidSpec() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUserName("test");
        final String tId = this.centralAuthenticationService.createTicketGrantingTicket(c);
        this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        final String sId2 = this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId2);
        request.addParameter(WebConstants.RENEW, "true");
        
        assertEquals(ViewNames.CONST_SERVICE_FAILURE, this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getViewName());
    }
    
    public void testInvalidServiceTicket() throws Exception {
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUserName("test");
        final String tId = this.centralAuthenticationService.createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        
        this.centralAuthenticationService.destroyTicketGrantingTicket(tId);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        
        assertEquals(ViewNames.CONST_SERVICE_FAILURE, this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getViewName());
    }
    
    public void testValidServiceTicketWithPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUserName("test");
        final String tId = this.centralAuthenticationService.createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        request.addParameter(WebConstants.PGTURL, "https://www.acs.rutgers.edu");
        
        assertEquals(ViewNames.CONST_SERVICE_SUCCESS, this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse()).getViewName());
    }
    
    public void testValidServiceTicketWithBadPgt() throws Exception {
        this.serviceValidateController.setProxyHandler(new Cas10ProxyHandler());
        UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        c.setPassword("test");
        c.setUserName("test");
        final String tId = this.centralAuthenticationService.createTicketGrantingTicket(c);
        final String sId = this.centralAuthenticationService.grantServiceTicket(tId, new SimpleService("test"));
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.TICKET, sId);
        request.addParameter(WebConstants.PGTURL, "http://www.acs.rutgers.edu");
        
        final ModelAndView modelAndView = this.serviceValidateController.handleRequestInternal(request, new MockHttpServletResponse());
        assertEquals(ViewNames.CONST_SERVICE_SUCCESS, modelAndView.getViewName());
        assertNull(modelAndView.getModel().get(WebConstants.PGTIOU));
    }
}
