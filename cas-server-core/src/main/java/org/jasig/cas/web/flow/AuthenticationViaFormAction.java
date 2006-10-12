/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

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

    private ArgumentExtractor[] argumentExtractors;

    private CookieGenerator warnCookieGenerator;

    private CookieGenerator ticketGrantingTicketCookieGenerator;

    protected final void doBind(final RequestContext context,
        final DataBinder binder) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        final Credentials credentials = (Credentials) binder.getTarget();

        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }

        super.doBind(context, binder);
    }

    public final Event submit(final RequestContext context) throws Exception {
        final Credentials credentials = (Credentials) getFormObject(context);
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        final String ticketGrantingTicketIdFromCookie = WebUtils
            .getCookieValue(request, this.ticketGrantingTicketCookieGenerator
                .getCookieName());
        final Service service = WebUtils.getService(this.argumentExtractors,
            WebUtils.getHttpServletRequest(context));

        if (StringUtils.hasText(request.getParameter("renew"))
            && ticketGrantingTicketIdFromCookie != null && service != null) {

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketIdFromCookie,
                        service, credentials);
                WebUtils.putServiceTicketInRequestScope(context,
                    serviceTicketId);
                putWarnCookieIfRequestParameterPresent(context);
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
            WebUtils.putTicketGrantingTicketInRequestScope(context,
                this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials));

            putWarnCookieIfRequestParameterPresent(context);
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

    private void putWarnCookieIfRequestParameterPresent(
        final RequestContext context) {
        final HttpServletResponse response = WebUtils
            .getHttpServletResponse(context);

        if (StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get("warn"))) {
            this.warnCookieGenerator.addCookie(response, "true");
        } else {
            this.warnCookieGenerator.removeCookie(response);
        }
    }

    public final void setArgumentExtractors(
        final ArgumentExtractor[] argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
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
    
    public final void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    
    public final void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    protected void initAction() {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null");
        Assert.notNull(this.argumentExtractors,
            "argumentExtractors cannot be null.");
        Assert.notNull(this.ticketGrantingTicketCookieGenerator,
            "ticketGrantingTicketCookieGenerator cannot be null.");
        Assert.notNull(this.warnCookieGenerator,
            "warnCookieGenerator cannot be null.");

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
