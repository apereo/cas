/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.support.HttpBasedServiceCredentialsAuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.HttpBasedServiceCredentialsToPrincipalResolver;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;

import junit.framework.TestCase;


public abstract class AbstractCentralAuthenticationServiceTest extends TestCase {
    protected CentralAuthenticationServiceImpl centralAuthenticationService;
    
    protected AuthenticationManagerImpl authenticationManager;

    public final CentralAuthenticationService getCentralAuthenticationService() throws Exception {
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl();
        this.authenticationManager = new AuthenticationManagerImpl();
        this.centralAuthenticationService
            .setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService
            .setTicketRegistry(new DefaultTicketRegistry());
        this.centralAuthenticationService
            .setUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());

        CredentialsToPrincipalResolver[] resolvers = new CredentialsToPrincipalResolver[] {new UsernamePasswordCredentialsToPrincipalResolver(), new HttpBasedServiceCredentialsToPrincipalResolver()};
        AuthenticationHandler[] handlers = new AuthenticationHandler[] {new SimpleTestUsernamePasswordAuthenticationHandler(), new HttpBasedServiceCredentialsAuthenticationHandler()};

        this.authenticationManager.setAuthenticationHandlers(handlers);
        this.authenticationManager.setCredentialsToPrincipalResolvers(resolvers);
        this.authenticationManager.afterPropertiesSet();

        this.centralAuthenticationService.setAuthenticationManager(this.authenticationManager);
        this.centralAuthenticationService.afterPropertiesSet();

        return this.centralAuthenticationService;
    }
}
