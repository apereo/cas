/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.FormAction;
import org.springframework.webflow.action.FormObjectAccessor;

/**
 * Action in flow of Login that attempts to collect and process credentials
 * related to any type of information that can be collected via form.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LoginFormAction extends FormAction {

    /** Log instance. */
    private Log log = LogFactory.getLog(this.getClass());

    /**
     * Binder that allows additional binding of form object beyond Spring
     * defaults.
     */
    private CredentialsBinder credentialsBinder;

    /** Core we delegate to for handling all ticket related tasks. */
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * Amount of time to keep the TicketGrantingTicket Cookie around for.
     * Negative values denote browser/session length, zero indicates destroy
     * ticket and a positive value is a time value in seconds.
     * <p>
     * Default value is -1
     */
    private int cookieTimeout = -1;

    protected void doBind(final RequestContext context, final DataBinder binder)
        throws Exception {
        super.doBind(context, binder);
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final Credentials credentials = (Credentials) binder.getTarget();
        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }
    }

    /**
     * This method submits the information to the CentralAuthenticationService.
     * It follows the following flow:
     * <ul>
     * <li> If renew is true and there is a TicketGrantingTicket, attempt to
     * generate a service ticket with the supplied credentials.</li>
     * <li> If we cannot create a ServiceTicket attempt to generate a new
     * TicketGrantingTicket.</li>
     * <li> If that is successful, store the value in a cookie.</li>
     * <li> Create a privacy cookie if needed.</li>
     * <li> Attempt to get a service ticket.</li>
     * </ul>
     * 
     * @return
     * <ul>
     * <li>"noService" event if there is successful authentication but no
     * service;</li>
     * <li>"warn" if there is successful authentication and a service provided.</li>
     * <li>"error" if authentication was unsuccessful.</li>
     * </ul>
     */
    public Event submit(final RequestContext context) throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(context);
        final boolean renew = request.getParameter(WebConstants.RENEW) != null;
        final boolean warn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));

        final String service = request.getParameter(WebConstants.SERVICE);
        final Credentials credentials = (Credentials) context.getRequestScope()
            .get(getFormObjectName());
        String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);
        String serviceTicketId = null;

        if (renew && StringUtils.hasText(ticketGrantingTicketId)
            && StringUtils.hasText(service)) {

            try {
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
            } catch (TicketException e) {
                this.centralAuthenticationService
                    .destroyTicketGrantingTicket(ticketGrantingTicketId);
                log
                    .debug(
                        "Attempted to generate a ServiceTicket using renew=true with different credentials",
                        e);
                // nothing to do here....move on.
            }
        }

        try {
            if (serviceTicketId == null) {
                ticketGrantingTicketId = this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials);
            }

            this.createCookie(WebConstants.COOKIE_TGC_ID,
                ticketGrantingTicketId, request, response);

            if (warn) {
                this
                    .createCookie(WebConstants.COOKIE_PRIVACY,
                        WebConstants.COOKIE_DEFAULT_FILLED_VALUE, request,
                        response);
            } else {
                this.createCookie(WebConstants.COOKIE_PRIVACY,
                    WebConstants.COOKIE_DEFAULT_EMPTY_VALUE, request, response);
            }

            if (StringUtils.hasText(service)) {
                if (serviceTicketId == null) {
                    serviceTicketId = this.centralAuthenticationService
                        .grantServiceTicket(ticketGrantingTicketId,
                            new SimpleService(service));
                }

                ContextUtils.addAttributeToFlowScope(context,
                    WebConstants.TICKET, serviceTicketId);
                ContextUtils.addAttributeToFlowScope(context,
                    WebConstants.SERVICE, service);

                return result("warn");
            }
        } catch (final TicketException e) {
            final FormObjectAccessor accessor = new FormObjectAccessor(context);
            final Errors errors = accessor.getFormErrors(this
                .getFormObjectName(), this.getFormErrorsScope());
            errors.reject(e.getCode(), e.getCode());
            return error();
        }

        return result("noService");
    }

    /**
     * Method to create a cookie and put it in the response.
     * 
     * @param id The id to name the cookie.
     * @param value The value to give the cookie.
     * @param request The HttpServletRequest
     * @param response TheHttpServletResponse to store the cookie.
     */
    private void createCookie(final String id, final String value,
        final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie cookie = new Cookie(id, value);
        cookie.setSecure(true);
        cookie.setMaxAge(this.cookieTimeout);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
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

    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        final String name = this.getClass().getName();

        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null on " + name);

        if (this.getFormObjectClass() == null) {
            this.setFormObjectClass(UsernamePasswordCredentials.class);
            this.setFormObjectName("credentials");
            this.setValidator(new UsernamePasswordCredentialsValidator());

            log.info("FormObjectClass not set.  Using default class of "
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

    public void setCookieTimeout(final int cookieTimeout) {
        this.cookieTimeout = cookieTimeout;
    }
}
