package org.jasig.cas.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.DefaultAuthenticationAttributesPopulator;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.DefaultCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.DefaultUniqueTokenIdGenerator;
import org.jasig.cas.web.bind.support.DefaultSpringBindCredentialsBinder;

import junit.framework.TestCase;


public class LoginControllerTests extends TestCase {

    private LoginController loginController;

    private CentralAuthenticationService getCentralAuthenticationService() {
        CentralAuthenticationServiceImpl centralAuthenticationService = new CentralAuthenticationServiceImpl();
        centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        centralAuthenticationService.setTicketRegistry(new DefaultTicketRegistry());
        centralAuthenticationService.setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        
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
        
        centralAuthenticationService.setAuthenticationManager(manager);
        
        return centralAuthenticationService;
    }
    
    protected void setUp() throws Exception {
        this.loginController = new LoginController();
        this.loginController.setCentralAuthenticationService(getCentralAuthenticationService());
        this.loginController.setLoginTokens(new HashMap());
        this.loginController.afterPropertiesSet();
        
    }
    
    public void testAfterPropertiesSet() throws Exception {
        this.loginController.setCredentialsBinder(new DefaultSpringBindCredentialsBinder());
        this.loginController.setUniqueTokenIdGenerator(new DefaultUniqueTokenIdGenerator());
        this.loginController.setFormView("test");
        this.loginController.setSuccessView("test");

        this.loginController.setCommandClass(UsernamePasswordCredentials.class);
        this.loginController.afterPropertiesSet();
    }
    
    public void testAfterPropertiesSetNoLoginTokens() {
      this.loginController.setLoginTokens(null);
        try {
            this.loginController.afterPropertiesSet();
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
}
