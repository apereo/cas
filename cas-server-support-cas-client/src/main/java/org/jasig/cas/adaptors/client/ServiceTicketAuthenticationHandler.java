/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.client;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidator;
import org.jasig.cas.client.validation.ValidationException;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class ServiceTicketAuthenticationHandler implements
    AuthenticationHandler {
    
    private TicketValidator ticketValidator;
    
    private Service service;

    public boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        final ServiceTicketCredentials c = (ServiceTicketCredentials) credentials;
        try {
            final Assertion assertion = this.ticketValidator.validate(c.getServiceTicketId(), this.service);
            c.setAssertion(assertion);
            return true;
        } catch (final ValidationException e) {
            return false;
        }        
    }

    public boolean supports(final Credentials credentials) {
        return credentials != null && ServiceTicketCredentials.class.isAssignableFrom(credentials.getClass());
    }
    
    public void setService(final String service) {
        this.service = new SimpleService(service);
    }
    
    public void setTicketValidator(final TicketValidator ticketValidator) {
        this.ticketValidator = ticketValidator;
    }

}
