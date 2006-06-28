/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;

/**
 * Common utilities for extracting information from the RequestContext.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ContextUtils {

    private ContextUtils() {
        // private constructor so that we can't instanciate new instances.
    }

    /**
     * Method to retrieve the HttpServletRequest from a RequestContext that
     * originated from a HttpServletRequestEvent.
     * 
     * @param context the RequestContext to grab the HttpServletRequest from.
     * @return the HttpServletRequest for this context.
     * @throws IllegalStateException if the originating event was not a
     * HttpServletRequestEvent
     */
    public static HttpServletRequest getHttpServletRequest(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext()).getRequest();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletRequest from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    /**
     * Method to retrieve the HttpServletResponse from a RequestContext that
     * originated from a HttpServletRequestEvent.
     * 
     * @param context the RequestContext to grab the HttpServletResponse from.
     * @return the HttpServletResponse for this context.
     * @throws IllegalStateException if the originating event was not a
     * HttpServletRequestEvent
     */
    public static HttpServletResponse getHttpServletResponse(
        final RequestContext context) {
        if (context.getExternalContext().getClass().equals(ServletExternalContext.class)) {
            return ((ServletExternalContext) context.getExternalContext()).getResponse();
        }

        throw new IllegalStateException(
            "Cannot obtain HttpServletResponse from event of type: "
                + context.getExternalContext().getClass().getName());
    }

    /**
     * Convenience method to add an attribute to the Request scope.
     * 
     * @param context the RequestContext to add the attribute to.
     * @param attributeName The name of the attribute.
     * @param attribute the value of the attribute.
     */
    public static void addAttribute(final RequestContext context,
        final String attributeName, final Object attribute) {
        context.getRequestScope().put(attributeName, attribute);
    }

    /**
     * Convenience method to retrieve an attribute from the Request scope.
     * 
     * @param context the RequestContext to retrieve the attribute from.
     * @param attributeName The name of the attribute.
     * @return the value of the attribute.
     */
    public static Object getAttribute(final RequestContext context,
        final String attributeName) {
        return context.getRequestScope().get(attributeName);
    }
    
    public static String getParameterAsString(final RequestContext context, final String parameterName) {
        return context.getExternalContext().getRequestParameterMap().get(parameterName);
    }
    
    public static boolean getParameterAsBoolean(final RequestContext context, final String parameterName) {
        return StringUtils.hasText(context.getExternalContext().getRequestParameterMap().get(parameterName));
    }
}
