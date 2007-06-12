/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import java.util.List;

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
 * @version $Revision$ $Date$
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
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
        return ((ServletExternalContext) context.getExternalContext())
            .getResponse();
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
        final List<ArgumentExtractor> argumentExtractors,
        final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        return getService(argumentExtractors, request);
    }

    public static final WebApplicationService getService(
        final RequestContext context) {
        return (WebApplicationService) context.getFlowScope().get("service");
    }

    public static final void putTicketGrantingTicketInRequestScope(
        final RequestContext context, final String ticketValue) {
        context.getRequestScope().put("ticketGrantingTicketId", ticketValue);
    }

    public static final String getTicketGrantingTicketId(
        final RequestContext context) {
        final String tgtFromRequest = context.getRequestScope().getString("ticketGrantingTicketId");
        final String tgtFromFlow = context.getFlowScope().getString("ticketGrantingTicketId");
        
        return tgtFromRequest != null ? tgtFromRequest : tgtFromFlow;

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
