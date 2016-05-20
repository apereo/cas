/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.util.Assert;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;

/**
 * Common utilities for the web tier.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class WebUtils {
    /** Request attribute that contains message key describing details of authorization failure.*/
    public static final String CAS_ACCESS_DENIED_REASON = "CAS_ACCESS_DENIED_REASON";

    /**
     * Instantiates a new web utils instance.
     */
    private WebUtils() {}

    /**
     * Gets the http servlet request from the context.
     *
     * @param context the context
     * @return the http servlet request
     */
    public static HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());

        return (HttpServletRequest) context.getExternalContext().getNativeRequest();
    }

    /**
     * Gets the http servlet response.
     *
     * @param context the context
     * @return the http servlet response
     */
    public static HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
        return (HttpServletResponse) context.getExternalContext()
            .getNativeResponse();
    }

    /**
     * Gets the service from the request based on given extractors.
     *
     * @param argumentExtractors the argument extractors
     * @param request the request
     * @return the service, or null.
     */
    public static WebApplicationService getService(
        final List<ArgumentExtractor> argumentExtractors,
        final HttpServletRequest request) {
        for (final ArgumentExtractor argumentExtractor : argumentExtractors) {
            final WebApplicationService service = argumentExtractor
                .extractService(request);

            if (service != null) {
                return service;
            }
        }

        return null;
    }

    /**
     * Gets the service.
     *
     * @param argumentExtractors the argument extractors
     * @param context the context
     * @return the service
     */
    public static WebApplicationService getService(
        final List<ArgumentExtractor> argumentExtractors,
        final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        return getService(argumentExtractors, request);
    }

    /**
     * Gets the service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static WebApplicationService getService(final RequestContext context) {
        return context != null ? (WebApplicationService) context.getFlowScope().get("service") : null;
    }

    /**
     * Gets the registered service from the flow scope.
     *
     * @param context the context
     * @return the service
     */
    public static RegisteredService getRegisteredService(final RequestContext context) {
        return context != null ? (RegisteredService) context.getFlowScope().get("registeredService") : null;
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticket the ticket value
     */
    public static void putTicketGrantingTicketInScopes(
        final RequestContext context, @NotNull final TicketGrantingTicket ticket) {
        final String ticketValue = ticket != null ? ticket.getId() : null;
        putTicketGrantingTicketInScopes(context, ticketValue);
    }

    /**
     * Put ticket granting ticket in request and flow scopes.
     *
     * @param context the context
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketInScopes(
            final RequestContext context, @NotNull final String ticketValue) {
        putTicketGrantingTicketIntoMap(context.getRequestScope(), ticketValue);
        putTicketGrantingTicketIntoMap(context.getFlowScope(), ticketValue);
    }

    /**
     * Put ticket granting ticket into map that is either backed by the flow/request scope.
     * Will override the previous value and blank out the setting if value is null or empty.
     * @param map the map
     * @param ticketValue the ticket value
     */
    public static void putTicketGrantingTicketIntoMap(final MutableAttributeMap map,
                                                       @NotNull final String ticketValue) {
        map.put("ticketGrantingTicketId", ticketValue);
    }

    /**
     * Gets the ticket granting ticket id from the request and flow scopes.
     *
     * @param context the context
     * @return the ticket granting ticket id
     */
    public static String getTicketGrantingTicketId(
            @NotNull final RequestContext context) {
        final String tgtFromRequest = (String) context.getRequestScope().get("ticketGrantingTicketId");
        final String tgtFromFlow = (String) context.getFlowScope().get("ticketGrantingTicketId");

        return tgtFromRequest != null ? tgtFromRequest : tgtFromFlow;

    }

    /**
     * Put service ticket in request scope.
     *
     * @param context the context
     * @param ticketValue the ticket value
     */
    public static void putServiceTicketInRequestScope(
        final RequestContext context, final ServiceTicket ticketValue) {
        context.getRequestScope().put("serviceTicketId", ticketValue.getId());
    }

    /**
     * Gets the service ticket from request scope.
     *
     * @param context the context
     * @return the service ticket from request scope
     */
    public static String getServiceTicketFromRequestScope(
        final RequestContext context) {
        return context.getRequestScope().getString("serviceTicketId");
    }

    /**
     * Put login ticket into flow scope.
     *
     * @param context the context
     * @param ticket the ticket
     */
    public static void putLoginTicket(final RequestContext context, final String ticket) {
        context.getFlowScope().put("loginTicket", ticket);
    }

    /**
     * Put unauthorized URL into flow scope.
     * @param context request context
     * @param url unauthorized url
     */
    public static void putUnauthorizedRedirectUrlIntoFlowScope(final RequestContext context, final URI url) {
        context.getFlowScope().put("unauthorizedRedirectUrl", url);
    }

    /**
     * Gets the login ticket from flow scope.
     *
     * @param context the context
     * @return the login ticket from flow scope
     */
    public static String getLoginTicketFromFlowScope(final RequestContext context) {
        // Getting the saved LT destroys it in support of one-time-use
        // See section 3.5.1 of http://www.jasig.org/cas/protocol
        final String lt = (String) context.getFlowScope().remove("loginTicket");
        return lt != null ? lt : "";
    }

    /**
     * Gets the login ticket from request.
     *
     * @param context the context
     * @return the login ticket from request
     */
    public static String getLoginTicketFromRequest(final RequestContext context) {
       return context.getRequestParameters().get("lt");
    }

    /**
     * Put logout requests into flow scope.
     *
     * @param context the context
     * @param requests the requests
     */
    public static void putLogoutRequests(final RequestContext context, final List<LogoutRequest> requests) {
        context.getFlowScope().put("logoutRequests", requests);
    }

    /**
     * Gets the logout requests from flow scope.
     *
     * @param context the context
     * @return the logout requests
     */
    public static List<LogoutRequest> getLogoutRequests(final RequestContext context) {
        return (List<LogoutRequest>) context.getFlowScope().get("logoutRequests");
    }

    /**
     * Put service into flowscope.
     *
     * @param context the context
     * @param service the service
     */
    public static void putService(final RequestContext context, final Service service) {
        context.getFlowScope().put("service", service);
    }

    /**
     * Put warning cookie value into flowscope.
     *
     * @param context the context
     * @param cookieValue the cookie value
     */
    public static void putWarningCookie(final RequestContext context, final Boolean cookieValue) {
        context.getFlowScope().put("warnCookieValue", cookieValue);
    }

    /**
     * Put registered service into flowscope.
     *
     * @param context the context
     * @param registeredService the service
     */
    public static void putRegisteredService(final RequestContext context,
                                            final RegisteredService registeredService) {
        context.getFlowScope().put("registeredService", registeredService);
    }

    /**
     * Gets credential from the context.
     *
     * @param context the context
     * @return the credential, or null if it cant be found in the context or if it has no id.
     */
    public static Credential getCredential(@NotNull final RequestContext context) {
        final Credential cFromRequest = (Credential) context.getRequestScope().get("credential");
        final Credential cFromFlow = (Credential) context.getFlowScope().get("credential");

        final Credential credential = cFromRequest != null ? cFromRequest : cFromFlow;
        if (credential != null && StringUtils.isBlank(credential.getId())) {
            return null;
        }
        return credential;
    }

    /**
     * Put unauthorized redirect url into flowscope.
     *
     * @param context the context
     * @param unauthorizedRedirectUrl the url to redirect to
     */
    public static void putUnauthorizedRedirectUrl(final RequestContext context,
                                            final URI unauthorizedRedirectUrl) {
        context.getFlowScope().put("unauthorizedRedirectUrl", unauthorizedRedirectUrl);
    }
}
