/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * Common utilities for the web tier.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class WebUtils {

    public static final HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(
            ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext())
                .getRequest();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    public static final HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(
            ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext())
                .getResponse();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    public static final String getCookieValue(final HttpServletRequest request,
        final String cookieName) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(
            request, cookieName);

        if (cookie == null) {
            return null;
        }

        return cookie.getValue();
    }

    public static final Service getService(
        final ArgumentExtractor[] argumentExtractors,
        final HttpServletRequest request) {
        for (int i = 0; i < argumentExtractors.length; i++) {
            final Service service = argumentExtractors[i]
                .extractService(request);

            if (service != null) {
                return service;
            }
        }

        return null;
    }
    
    public static final Service getService(final RequestContext context) {
        return (Service) context.getFlowScope().get("service");
    }

    public static final String getTicket(
        final ArgumentExtractor[] argumentExtractors,
        final HttpServletRequest request) {
        for (int i = 0; i < argumentExtractors.length; i++) {
            final String ticket = argumentExtractors[i]
                .extractTicketArtifact(request);

            if (ticket != null) {
                return ticket;
            }
        }

        return null;
    }

    public static final void putTicketGrantingTicketInRequestScope(
        final RequestContext context, final String ticketValue) {
        context.getRequestScope().put("ticketGrantingTicketId", ticketValue);
    }

    public static final String getTicketGrantingTicketFromRequestScope(
        final RequestContext context) {
        return context.getRequestScope().getString("ticketGrantingTicketId");
    }

    public static final void putServiceTicketInRequestScope(
        final RequestContext context, final String ticketValue) {
        context.getRequestScope().put("serviceTicketId", ticketValue);
    }

    public static final String getServiceTicketFromRequestScope(
        final RequestContext context) {
        return context.getRequestScope().getString("serviceTicketId");
    }
}
