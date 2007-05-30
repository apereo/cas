/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.springframework.util.Assert;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.RequestContext;

/**
 * Common utilities for the web tier.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date: 2007-02-27 14:35:37 -0500 (Tue, 27 Feb
 * 2007) $
 * @since 3.1
 */
public final class WebUtils {

    public static final HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());

        return ((ServletExternalContext) context.getExternalContext())
            .getRequest();
    }

    public static final HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());
        return ((ServletExternalContext) context.getExternalContext())
            .getResponse();
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

    public static final WebApplicationService getService(
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

    public static final WebApplicationService getService(
        final RequestContext context) {
        return (WebApplicationService) context.getFlowScope().get("service");
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
