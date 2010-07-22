/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * The NoSuchFlowExecutionResolver catches the NoSuchFlowExecutionException
 * thrown by Spring Webflow when the given flow id no longer exists. This can
 * occur if a particular flow has reached an end state (the id is no longer
 * valid)
 * <p>
 * It will redirect back to the requested URI which should start a new workflow.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class NoSuchFlowExecutionExceptionResolver implements
    HandlerExceptionResolver {

    /** Instance of a log. */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ModelAndView resolveException(final HttpServletRequest request,
        final HttpServletResponse response, final Object handler,
        final Exception exception) {

        if (!exception.getClass().equals(NoSuchFlowExecutionException.class)) {
            return null;
        }

        final String urlToRedirectTo = request.getRequestURI()
            + (request.getQueryString() != null ? "?"
                + request.getQueryString() : "");

        if (log.isDebugEnabled()) {
            log.debug("Error getting flow information for URL:"
                + urlToRedirectTo, exception);
        }

        return new ModelAndView(new RedirectView(urlToRedirectTo));
    }
}
