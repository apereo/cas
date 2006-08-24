/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.CasArgumentExtractor;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.springframework.util.Assert;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormAction;

/**
 * Action to authenticate credentials and retrieve a TicketGrantingTicket for
 * those credentials. If there is a request for renew, then it also generates
 * the Service Ticket required.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class AuthenticationViaFormAction extends FormAction {

    /**
     * Binder that allows additional binding of form object beyond Spring
     * defaults.
     */
    private CredentialsBinder credentialsBinder;

    /** Core we delegate to for handling all ticket related tasks. */
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * CasArgumentExtractor so that actions don't need to know about cookie
     * generators, etc.
     */
    private CasArgumentExtractor casArgumentExtractor;

    protected final void doBind(final RequestContext context,
        final DataBinder binder) {
        final HttpServletRequest request = this.casArgumentExtractor
            .getHttpServletRequest(context);
        final Credentials credentials = (Credentials) binder.getTarget();
        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }

        super.doBind(context, binder);
    }

    public final Event submit(final RequestContext context) throws Exception {
        final Credentials credentials = (Credentials) getFormObject(context);

        if (this.casArgumentExtractor.isRenewPresent(context)
            && this.casArgumentExtractor
                .isTicketGrantingTicketCookiePresent(context)
            && this.casArgumentExtractor.isServicePresent(context)) {

            final String ticketGrantingTicketIdFromCookie = this.casArgumentExtractor
                .extractTicketGrantingTicketFromCookie(context);

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketIdFromCookie,
                        this.casArgumentExtractor.extractServiceFrom(context),
                        credentials);
                this.casArgumentExtractor.putServiceTicketIn(context,
                    serviceTicketId);
                this.casArgumentExtractor
                    .putWarnCookieIfRequestParameterPresent(context);
                return warn();
            } catch (final TicketException e) {
                if (e.getCause() != null
                    && AuthenticationException.class.isAssignableFrom(e
                        .getCause().getClass())) {
                    populateErrorsInstance(context, e);
                    return error();
                }
                this.centralAuthenticationService
                    .destroyTicketGrantingTicket(ticketGrantingTicketIdFromCookie);
                if (logger.isDebugEnabled()) {
                    logger
                        .debug(
                            "Attempted to generate a ServiceTicket using renew=true with different credentials",
                            e);
                }
            }
        }

        try {
            this.casArgumentExtractor.putTicketGrantingTicketIn(context,
                this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials));
            this.casArgumentExtractor
                .putWarnCookieIfRequestParameterPresent(context);
            return success();
        } catch (final TicketException e) {
            populateErrorsInstance(context, e);
            return error();
        }
    }

    private final Event warn() {
        return result("warn");
    }

    private final void populateErrorsInstance(final RequestContext context,
        final TicketException e) {

        try {
            final Errors errors = getFormErrors(context);
            errors.reject(e.getCode(), e.getCode());
        } catch (final Exception fe) {
            logger.error(fe, fe);
        }
    }

    public final void setCasArgumentExtractor(
        final CasArgumentExtractor casArgumentExtractor) {
        this.casArgumentExtractor = casArgumentExtractor;
    }

    public final void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    /**
     * Set a CredentialsBinder for additional binding of the HttpServletRequest
     * to the Credentials instance, beyond our default binding of the
     * Credentials as a Form Object in Spring WebMVC parlance. By the time we
     * invoke this CredentialsBinder, we have already engaged in default binding
     * such that for each HttpServletRequest parameter, if there was a JavaBean
     * property of the Credentials implementation of the same name, we have set
     * that property to be the value of the corresponding request parameter.
     * This CredentialsBinder plugin point exists to allow consideration of
     * things other than HttpServletRequest parameters in populating the
     * Credentials (or more sophisticated consideration of the
     * HttpServletRequest parameters).
     */
    public final void setCredentialsBinder(
        final CredentialsBinder credentialsBinder) {
        this.credentialsBinder = credentialsBinder;
    }

    protected void initAction() {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null");
        Assert.notNull(this.casArgumentExtractor,
            "casArgumentExtractor canont be null.");

        if (this.getFormObjectClass() == null) {
            this.setFormObjectClass(UsernamePasswordCredentials.class);
            this.setFormObjectName("credentials");
            this.setValidator(new UsernamePasswordCredentialsValidator());

            logger.info("FormObjectClass not set.  Using default class of "
                + this.getFormObjectClass().getName() + " with formObjectName "
                + this.getFormObjectName() + " and validator "
                + this.getValidator().getClass().getName() + ".");
        }

        Assert
            .isTrue(Credentials.class.isAssignableFrom(this
                .getFormObjectClass()),
                "CommandClass must be of type Credentials.");

        if (this.credentialsBinder != null) {
            Assert.isTrue(this.credentialsBinder.supports(this
                .getFormObjectClass()),
                "CredentialsBinder does not support supplied FormObjectClass: "
                    + this.getClass().getName());
        }
    }
}
