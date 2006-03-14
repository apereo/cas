/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.remoting.server;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.Assertion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * Wrapper implementation around a CentralAuthenticationService that does
 * completes the marshalling of parameters from the web-service layer to the
 * service layer. Typically the only thing that is done is to validate the
 * parameters (as you would in the web tier) and then delegate to the service
 * layer.
 * <p>
 * The following properties are required:
 * </p>
 * <ul>
 * <li>centralAuthenticationService - the service layer we are delegating to.</li>
 * </ul>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RemoteCentralAuthenticationService implements
    CentralAuthenticationService, InitializingBean {

    /** The CORE to delegate to. */
    private CentralAuthenticationService centralAuthenticationService;

    /** The validators to check the Credentials. */
    private Validator[] validators;

    /**
     * @throws IllegalArgumentException if the Credentials are null or if given
     * invalid credentials.
     */
    public String createTicketGrantingTicket(final Credentials credentials)
        throws TicketException {
        Assert.notNull(credentials, "credentials cannot be null");

        final Errors errors = validateCredentials(credentials);
        if (errors.hasErrors()) {
            throw new IllegalArgumentException("Error validating credentials: "
                + errors.toString());
        }

        return this.centralAuthenticationService
            .createTicketGrantingTicket(credentials);
    }

    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service) throws TicketException {

        return this.centralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, service);
    }

    /**
     * @throws IllegalArgumentException if given invalid credentials
     */
    public String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketException {

        if (credentials != null) {
            final Errors errors = validateCredentials(credentials);
            if (errors.hasErrors()) {
                throw new IllegalArgumentException(
                    "Error validating credentials: " + errors.toString());
            }
        }

        return this.centralAuthenticationService.grantServiceTicket(
            ticketGrantingTicketId, service, credentials);
    }

    public Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException {
        return this.centralAuthenticationService.validateServiceTicket(
            serviceTicketId, service);
    }

    public void destroyTicketGrantingTicket(final String ticketGrantingTicketId) {
        this.centralAuthenticationService
            .destroyTicketGrantingTicket(ticketGrantingTicketId);
    }

    /**
     * @throws IllegalArgumentException if the credentials are invalid.
     */
    public String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws TicketException {

        final Errors errors = validateCredentials(credentials);
        if (errors.hasErrors()) {
            throw new IllegalArgumentException("Error validating credentials: "
                + errors.toString());
        }

        return this.centralAuthenticationService.delegateTicketGrantingTicket(
            serviceTicketId, credentials);
    }

    private Errors validateCredentials(final Credentials credentials) {
        final Errors errors = new BindException(credentials, "credentials");

        if (this.validators == null) {
            return errors;
        }

        for (int i = 0; i < this.validators.length; i++) {
            if (this.validators[i].supports(credentials.getClass())) {
                ValidationUtils.invokeValidator(this.validators[i],
                    credentials, errors);
            }
        }

        return errors;
    }

    /**
     * Set the CentralAuthenticationService.
     * 
     * @param centralAuthenticationService The CentralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Set the list of validators.
     * 
     * @param validators The array of validators to use.
     */
    public void setValidators(final Validator[] validators) {
        this.validators = validators;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService is a required property.");
    }
}
