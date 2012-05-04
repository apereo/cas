/*
 * Copyright 2012 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.jasig.org/cas/license.
 */
package org.jasig.cas.security.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Web authentication entry point that simply passes through without any authentication operation.
 * This type of entry point is useful for IP-based filtering using the Spring EL <code>hasIpAddress(...)</code>
 * expression.
 *
 * @author Middleware Services
 * @version $Revision: $
 */
public final class PassThroughAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Permits the request to proceed in all cases.
     *
     * @param request Servlet request.
     * @param response Servlet response.
     * @param authException Authentication exception from higher up in filter chain.
     * @throws IOException Never thrown.
     * @throws ServletException Never thrown.
     */
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException)
            throws IOException, ServletException {

        logger.debug("Permitting {} to pass through", request.getRemoteAddr());
    }
}
