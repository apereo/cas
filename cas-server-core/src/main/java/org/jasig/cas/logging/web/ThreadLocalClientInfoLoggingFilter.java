/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.logging.web;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.logging.ClientInfo;
import org.jasig.cas.logging.ClientInfoHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that exposes ClientInfo to the Event handling code for logging/auditing purposes.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1.2
 *
 */
public class ThreadLocalClientInfoLoggingFilter extends OncePerRequestFilter {

    protected void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain filterChain) throws ServletException,
        IOException {
        
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            ClientInfoHolder.clear();
        }
    }
}
