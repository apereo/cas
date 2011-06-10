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

    public static HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());

        return (HttpServletRequest) context.getExternalContext().getNativeRequest();
    }

    public static HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        Assert.isInstanceOf(ServletExternalContext.class, context
            .getExternalContext(),
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
        return (HttpServletResponse) context.getExternalContext()
            .getNativeResponse();
    }

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
    
    public static WebApplicationService getService(
        final List<ArgumentExtractor> argumentExtractors,
        final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        return getService(argumentExtractors, request);
    }

    public static WebApplicationService getService(
        final RequestContext context) {
        return (WebApplicationService) context.getFlowScope().get("service");
    }

    public static void putTicketGrantingTicketInRequestScope(
        final RequestContext context, final String ticketValue) {
        context.getRequestScope().put("ticketGrantingTicketId", ticketValue);
    }

    public static String getTicketGrantingTicketId(
        final RequestContext context) {
        final String tgtFromRequest = (String) context.getRequestScope().get("ticketGrantingTicketId");
        final String tgtFromFlow = (String) context.getFlowScope().get("ticketGrantingTicketId");
        
        return tgtFromRequest != null ? tgtFromRequest : tgtFromFlow;

    }

    public static void putServiceTicketInRequestScope(
        final RequestContext context, final String ticketValue) {
        context.getRequestScope().put("serviceTicketId", ticketValue);
    }

    public static String getServiceTicketFromRequestScope(
        final RequestContext context) {
        return context.getRequestScope().getString("serviceTicketId");
    }
    
    public static void putLoginTicket(final RequestContext context, final String ticket) {
        context.getFlowScope().put("loginTicket", ticket);
    }
    
    public static String getLoginTicketFromFlowScope(final RequestContext context) {
        // Getting the saved LT destroys it in support of one-time-use
        // See section 3.5.1 of http://www.jasig.org/cas/protocol
        final String lt = (String) context.getFlowScope().remove("loginTicket");
        return lt != null ? lt : "";
    }
    
    public static String getLoginTicketFromRequest(final RequestContext context) {
       return context.getRequestParameters().get("lt");
    }
}
