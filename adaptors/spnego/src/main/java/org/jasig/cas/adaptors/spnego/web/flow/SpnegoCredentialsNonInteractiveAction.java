/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSContext;
import org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.adaptors.spnego.util.SpnegoUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public class SpnegoCredentialsNonInteractiveAction extends
    AbstractNonInteractiveCredentialsAction {

    private static final String HEADER_AUTHORIZATION = "Authorization";

    protected Credentials constructCredentialsFromRequest(
        final RequestContext context) {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(context);

        final String authorizationHeader = request
            .getHeader(HEADER_AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Negotiate ") || authorizationHeader.length() <= 10) {
            if (logger.isDebugEnabled()) {
                logger
                    .debug("Authorization header not found.  Sending WWW-Authenticate header");
            }
            response.setHeader("WWW-Authenticate", "Negotiate");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("GSSAPI Authorization header found with "
                + (authorizationHeader.length() - 10) + " bytes");
        }

        final byte[] token = Base64.decodeBase64(authorizationHeader
            .substring(10).getBytes());
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained token: " + token);
        }
        final GSSContext gssContext = SpnegoUtils.getContext(token);
        final byte[] outputToken = SpnegoUtils.getToken(token, gssContext);

        if (outputToken == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to obtain the output token required.");
            }

            response.setHeader("WWW-Authenticate", "Negotiate");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained output token:" + outputToken);
        }
        
        response.setHeader("WWW-Authenticate", "Negotiate "
            + outputToken);
        return new SpnegoCredentials(gssContext);
    }
}
