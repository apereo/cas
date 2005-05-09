/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.Date;
import java.util.Map;

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
import org.jasig.cas.util.DefaultUniqueTokenIdGenerator;
import org.jasig.cas.util.UniqueTokenIdGenerator;
import org.jasig.cas.validation.UsernamePasswordCredentialsValidator;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.bind.support.DefaultSpringBindCredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.FormAction;
import org.springframework.web.flow.action.FormObjectAccessor;

/**
 * Action in flow of Login that attempts to collect and process credentials
 * related to any type of information that can be collected via form.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class LogonFormAction extends FormAction {

    /** Log instance. */
    private Log log = LogFactory.getLog(this.getClass());

    /** Map of tokens used to prevent resubmission of a form. */
    private Map loginTokens;

    /** Id generator of tokens used to prevent resubmission of a form. */
    private UniqueTokenIdGenerator uniqueTokenIdGenerator;

    /** Binder that allows additional binding of form object beyond Spring defaults. */
    private CredentialsBinder credentialsBinder;

    /** Core we delegate to for handling all ticket related tasks. */
    private CentralAuthenticationService centralAuthenticationService;

    public Event setupReferenceData(final RequestContext context)
        throws Exception {
        ContextUtils.addAttribute(context, "loginToken", getLoginToken());
        return success();
    }

    protected void onBind(final RequestContext context,
        final Object formObject, final BindException errors) {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final Credentials credentials = (Credentials) formObject;
        this.credentialsBinder.bind(request, credentials);
    }

    public Event submit(final RequestContext context) throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponseFromContext(context);
        final String loginToken = request
            .getParameter(WebConstants.LOGIN_TOKEN);
        final boolean renew = Boolean.valueOf(
            request.getParameter(WebConstants.RENEW)).booleanValue();
        final boolean warn = StringUtils.hasText(request
            .getParameter(WebConstants.WARN));

        final String service = request.getParameter(WebConstants.SERVICE);
        final Credentials credentials = (Credentials) context.getRequestScope()
            .get(getFormObjectName());
        String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);
        String serviceTicketId = null;

        synchronized (this.loginTokens) {
            // check for a login ticket
            if (loginToken == null || !this.loginTokens.containsKey(loginToken)) {
                return error();
            }

            this.loginTokens.remove(loginToken);
        }

        if (renew && StringUtils.hasText(ticketGrantingTicketId)
            && StringUtils.hasText(service)) {

            try {
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
            } catch (TicketException e) {
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
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service));

                context.getFlowScope().setAttribute(WebConstants.TICKET,
                    serviceTicketId);
                context.getFlowScope().setAttribute(WebConstants.SERVICE,
                    service);

                return success();
            }
        } catch (final TicketException e) {
            final FormObjectAccessor accessor = new FormObjectAccessor(context);
            final Errors errors = accessor.getFormErrors(this
                .getFormObjectName(), this.getErrorsScope());
            errors.reject(e.getCode(), e.getCode());
            return error();
        }

        return result("noService");
    }

    /**
     * Generate a unique LoginToken string and save it in the Map.
     */
    private String getLoginToken() {
        final String loginToken = this.uniqueTokenIdGenerator.getNewTokenId();
        synchronized (this.loginTokens) {
            this.loginTokens.put(loginToken, new Date());
        }
        return loginToken;
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
        cookie.setMaxAge(-1);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }

    public void setCentralAuthenticationService(
        CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setCredentialsBinder(CredentialsBinder credentialsBinder) {
        this.credentialsBinder = credentialsBinder;
    }

    public void setLoginTokens(Map loginTokens) {
        this.loginTokens = loginTokens;
    }

    public void setUniqueTokenIdGenerator(
        UniqueTokenIdGenerator uniqueTokenIdGenerator) {
        this.uniqueTokenIdGenerator = uniqueTokenIdGenerator;
    }

    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        if (this.loginTokens == null
            || this.centralAuthenticationService == null) {
            throw new IllegalStateException(
                "You must set loginTokens and centralAuthenticationService on "
                    + this.getClass().getName());
        }

        if (this.uniqueTokenIdGenerator == null) {
            this.uniqueTokenIdGenerator = new DefaultUniqueTokenIdGenerator();
            log
                .info("UniqueTokenIdGenerator not set, using default UniqueIdGenerator of "
                    + this.uniqueTokenIdGenerator.getClass().getName());
        }

        if (this.getFormObjectClass() == null) {
            this.setFormObjectClass(UsernamePasswordCredentials.class);
            this.setFormObjectName("credentials");
            this.setValidator(new UsernamePasswordCredentialsValidator());

            log.info("FormObjectClass not set.  Using default class of "
                + this.getFormObjectClass().getName() + " with formObjectName "
                + this.getFormObjectName() + " and validator "
                + this.getValidator().getClass().getName() + ".");
        }

        if (this.credentialsBinder == null) {
            this.credentialsBinder = new DefaultSpringBindCredentialsBinder();
            log.info("CredentialsBinder not set.  Using default of "
                + this.credentialsBinder.getClass().getName());
        }

        if (!this.credentialsBinder.supports(this.getFormObjectClass())) {
            throw new IllegalStateException(
                "CredentialsBinder does not support supplied FormObjectClass: "
                    + this.getClass().getName());
        }
    }

}
