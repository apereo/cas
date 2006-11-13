/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.AssertionImpl;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public class ServiceTicketAuthenticationHandlerTests extends TestCase {

    private ServiceTicketAuthenticationHandler authenticationHandler;

    protected void setUp() throws Exception {
        this.authenticationHandler = new ServiceTicketAuthenticationHandler();
        this.authenticationHandler.setService("localhost");
        this.authenticationHandler.setTicketValidator(new TicketValidator(){

            public Assertion validate(final String ticketId,
                final Service service) throws ValidationException {
                if (ticketId.equals("yes")) {
                    return new AssertionImpl(new SimplePrincipal("test"));
                }

                throw new ValidationException("e");
            }
        });
    }

    public void testSuccessfulValidation() throws AuthenticationException {
        final String ticketId = "yes";
        final ServiceTicketCredentials serviceTicketCredentials = new ServiceTicketCredentials(
            ticketId);

        assertTrue(this.authenticationHandler
            .authenticate(serviceTicketCredentials));
    }
    
    public void testFailedValidation() throws AuthenticationException {
        final String ticketId = "no";
        final ServiceTicketCredentials serviceTicketCredentials = new ServiceTicketCredentials(
            ticketId);

        assertFalse(this.authenticationHandler
            .authenticate(serviceTicketCredentials));
    }
    
    public void testSupports() {
        assertTrue(this.authenticationHandler.supports(new ServiceTicketCredentials("test")));
        assertFalse(this.authenticationHandler.supports(new UsernamePasswordCredentials()));
    }
}
