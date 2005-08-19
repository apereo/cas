/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.bind.BindUtils;
import org.springframework.webflow.RequestContext;

/**
 * Webflow Action that allows for the non-interactive check for a credential.
 * Specific uses may include the checking for Client Certificates. Developers
 * need only supply a Credentials class and a custom CredentialsBinder. The
 * class will them bind the Credentials using both Spring binding and the custom
 * CredentialsBinder. It will attempt to obtain a TicketGrantingTicket and a
 * ServiceTicket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractNonInteractiveLoginAction extends AbstractCasAction
    implements InitializingBean {

    /** If no domainObjectName is provided, use the defaul. */
    private static final String DEFAULT_DOMAIN_OBJECT_NAME = "credentials";

    /** The class of the domain object. */
    private Class domainObjectClass;

    /** The name given to the domain object. */
    private String domainObjectName;

    /**
     * Binder that allows additional binding of form object beyond Spring
     * defaults.
     */
    private CredentialsBinder credentialsBinder;

    /** Core we delegate to for handling all ticket related tasks. */
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * Method to determine if the credentials we are expecting were provided via
     * the Request Context. This makes no statement about the validity of the
     * credentials just their existance.
     * 
     * @param requestContext the request context for this flow
     * @return true if the credentials were found in the request, false
     * otherwise.
     */
    protected abstract boolean credentialsExist(
        final RequestContext requestContext);

    /**
     * Method follows the following workflow:
     * <ul>
     * <li> call <code>credentailsExist(requestContext)</li>
     * <li> if false, return error event, otherwise...</li>
     * <li> check to see that we have a service. </li>
     * <li> Attempt to obtain the TicketGrantingTicket</li>
     * <li> Attempt to obtain the Service Ticket.</li>
     * </ul>
     */
    protected final ModelAndEvent doExecuteInternal(
        final RequestContext requestContext, final Map attributes)
        throws Exception {

        if (!credentialsExist(requestContext)) {
            return new ModelAndEvent(error());
        }

        if (ContextUtils.getHttpServletRequest(requestContext).getParameter(
            WebConstants.SERVICE) == null) {
            return new ModelAndEvent(error());
        }

        final Credentials credentials = (Credentials) createFormObject(requestContext);

        bind(requestContext, credentials);

        try {
            final String ticketGrantingTicketId = obtainTicketGrantingTicket(
                requestContext, credentials);
            final String serviceTicketId = obtainServiceTicket(requestContext,
                credentials, ticketGrantingTicketId);

            Map model = new HashMap();

            model.put(WebConstants.SERVICE, ContextUtils.getHttpServletRequest(
                requestContext).getParameter(WebConstants.SERVICE));
            model.put(WebConstants.TICKET, serviceTicketId);

            return new ModelAndEvent(success(), model);

        } catch (TicketException e) {
            return new ModelAndEvent(error());
        }
    }

    /**
     * Method to instanciate a new instance of the domain object.
     * 
     * @param context the Request Context.
     * @return the new Credentials object.
     * @throws InstantiationException if there was an error instanciating.
     * @throws IllegalAccessException if there was a security issue accessing
     * the class.
     */
    protected final Object createFormObject(RequestContext context)
        throws InstantiationException, IllegalAccessException {
        return this.domainObjectClass.newInstance();
    }

    /**
     * Method that calls the CredentialsBinder in order to populate the
     * Credentials object.
     * 
     * @param requestContext the RequestContext for this flow request..
     * @param credentials the Credentials to bind to.
     */
    protected final void bind(final RequestContext requestContext,
        Credentials credentials) {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        BindUtils.bind(request, credentials, this.domainObjectName);

        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }
    }

    /**
     * Method to obtain a TicketGrantingTicket either from a cookie or from the
     * CentralAuthenticationService.
     * 
     * @param requestContext the Request Context for this flow.
     * @param credentials the Credentials to use to create a new
     * TicketGrantingTicket
     * @return the String identifier for the TicketGrantingTicket.
     * @throws TicketException if there is a problem creating the new
     * TicketGrantingTicket.
     */
    protected final String obtainTicketGrantingTicket(
        final RequestContext requestContext, final Credentials credentials)
        throws TicketException {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);

        if (ticketGrantingTicketId != null) {
            return ticketGrantingTicketId;
        }

        return this.centralAuthenticationService
            .createTicketGrantingTicket(credentials);
    }

    /**
     * Method attempts to obtain a service ticket first by checking if the renew
     * flag is set and then trying to use the existing TicketGrantingTicket.
     * Failing that, it atempts to create a new TicketGrantingTicket and use
     * that.
     * 
     * @param requestContext the request context for this scope
     * @param credentials the Credentials representing a future Principal.
     * @param ticketGrantingTicketId the TicketGrantingTicket id to use for
     * single-sign on.
     * @return the String identifier for a unique ServiceTicket
     * @throws TicketException if there was a problem creating a cookie.
     */
    protected final String obtainServiceTicket(
        final RequestContext requestContext, final Credentials credentials,
        final String ticketGrantingTicketId) throws TicketException {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(requestContext);
        final boolean renew = Boolean.valueOf(
            request.getParameter(WebConstants.RENEW)).booleanValue();
        final String service = request.getParameter(WebConstants.SERVICE);
        String serviceTicketId = null;

        if (renew) {
            try {
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
            } catch (TicketException e) {
                // do nothing
            }
        }

        if (serviceTicketId == null) {
            String newTicketGrantingTicketId = this.centralAuthenticationService
                .createTicketGrantingTicket(credentials);

            serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicketId, new SimpleService(
                    service));

            createCookie(WebConstants.COOKIE_TGC_ID, newTicketGrantingTicketId,
                request, response);
        }

        return serviceTicketId;
    }

    /**
     * Method to create a cookie and put it in the response.
     * 
     * @param id The id to name the cookie.
     * @param value The value to give the cookie.
     * @param request The HttpServletRequest
     * @param response TheHttpServletResponse to store the cookie.
     */
    private final void createCookie(final String id, final String value,
        final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie cookie = new Cookie(id, value);
        cookie.setSecure(true);
        cookie.setMaxAge(-1);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }

    public final void afterPropertiesSet() {
        super.afterPropertiesSet();

        final String name = this.getClass().getName();

        Assert.notNull(this.domainObjectClass,
            "Domain Object Class must be set on " + name);
        Assert.notNull(this.centralAuthenticationService,
            "CentralAuthenticationService must be set on " + name);

        if (this.domainObjectName == null) {
            this.domainObjectName = DEFAULT_DOMAIN_OBJECT_NAME;
        }

        afterPropertiesSetInternal();
    }

    /**
     * Template method to provide afterPropertiesSet capabilities to subclassing
     * implementations without having to rely on them calling
     * super.afterPropertiesSet();
     */
    protected void afterPropertiesSetInternal() {
        // override in subclasses
    }
}
