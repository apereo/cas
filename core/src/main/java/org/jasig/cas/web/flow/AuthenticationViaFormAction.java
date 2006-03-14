/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.action.FormObjectAccessor;

/**
 * Action to authenticate credentials and retrieve a TicketGrantingTicket for
 * those credentials. If there is a request for renew, then it also generates
 * the Service Ticket required.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class AuthenticationViaFormAction extends FormAction implements
    InitializingBean {

    /**
     * Binder that allows additional binding of form object beyond Spring
     * defaults.
     */
    private CredentialsBinder credentialsBinder;

    /** Core we delegate to for handling all ticket related tasks. */
    private CentralAuthenticationService centralAuthenticationService;

    /** Generator for Warning Cookie. */
    private CookieGenerator warnCookieGenerator;

    /** Generator for Ticket Granting Ticket Cookie. */
    private CookieGenerator ticketGrantingTicketCookieGenerator;

    protected void doBind(final RequestContext context, final DataBinder binder)
        throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final Credentials credentials = (Credentials) binder.getTarget();
        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }

        super.doBind(context, binder);
    }

    public Event submit(final RequestContext context) throws Exception {
        final Credentials credentials = (Credentials) getFormObject(context);
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(context);
        final boolean warn = WebUtils.getRequestParameterAsBoolean(request,
            WebConstants.WARN);
        final boolean renew = WebUtils.getRequestParameterAsBoolean(request,
            WebConstants.RENEW);
        final String service = WebUtils.getRequestParameterAsString(request,
            WebConstants.SERVICE);
        final String ticketGrantingTicketIdFromCookie = WebUtils
            .getCookieValue(request, this.ticketGrantingTicketCookieGenerator
                .getCookieName());

        if (renew && StringUtils.hasText(ticketGrantingTicketIdFromCookie)
            && StringUtils.hasText(service)) {

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketIdFromCookie,
                        new SimpleService(service), credentials);
                ContextUtils.addAttribute(context, WebConstants.TICKET,
                    serviceTicketId);
                setWarningCookie(response, warn);
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
            final String ticketGrantingTicketId = this.centralAuthenticationService
                .createTicketGrantingTicket(credentials);
            ContextUtils.addAttribute(context,
                AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET,
                ticketGrantingTicketId);
            setWarningCookie(response, warn);
            return success();
        } catch (final TicketException e) {
            populateErrorsInstance(context, e);
            return error();
        }
    }

    private Event warn() {
        return result("warn");
    }

    private void populateErrorsInstance(final RequestContext context,
        final TicketException e) {
        final FormObjectAccessor accessor = new FormObjectAccessor(context);
        final Errors errors = accessor.getFormErrors(this.getFormObjectName(),
            this.getFormErrorsScope());
        errors.reject(e.getCode(), e.getCode());

    }

    private void setWarningCookie(final HttpServletResponse response,
        final boolean warn) {
        if (warn) {
            this.warnCookieGenerator.addCookie(response, "true");
        } else {
            this.warnCookieGenerator.removeCookie(response);
        }

    }

    public void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    public void setCentralAuthenticationService(
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
    public void setCredentialsBinder(final CredentialsBinder credentialsBinder) {
        this.credentialsBinder = credentialsBinder;
    }

    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        Assert.notNull(this.centralAuthenticationService, "centralAuthenticationService cannot be null");
        Assert.notNull(this.warnCookieGenerator, "warnCookieGenerator cannot be null");
        Assert.notNull(this.ticketGrantingTicketCookieGenerator, "ticketGrantingTicketCookieGenerator cannot be null");

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

        if (this.credentialsBinder != null
            && !this.credentialsBinder.supports(this.getFormObjectClass())) {
            throw new IllegalStateException(
                "CredentialsBinder does not support supplied FormObjectClass: "
                    + this.getClass().getName());
        }
    }
}
