/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

/**
 * Common utilities for extracting information from the RequestContext.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ContextUtils {

    public static HttpServletRequest getHttpServletRequest(final RequestContext context) {
        if (context.getOriginatingEvent() instanceof HttpServletRequestEvent) {
           return ((HttpServletRequestEvent) context.getOriginatingEvent()).getRequest(); 
        }
        
        throw new IllegalStateException("Cannot obtain HttpServletRequest from event of type: " + context.getOriginatingEvent().getClass().getName());
    }
    
    public static HttpServletResponse getHttpServletResponseFromContext(final RequestContext context) {
        if (context.getOriginatingEvent() instanceof HttpServletRequestEvent) {
           return ((HttpServletRequestEvent) context.getOriginatingEvent()).getResponse(); 
        }
        
        throw new IllegalStateException("Cannot obtain HttpServletResponse from event of type: " + context.getOriginatingEvent().getClass().getName());
    }
    
    public static void addAttribute(final RequestContext context, final String attributeName, final Object attribute) {
        context.getRequestScope().setAttribute(attributeName, attribute);
    }
}
