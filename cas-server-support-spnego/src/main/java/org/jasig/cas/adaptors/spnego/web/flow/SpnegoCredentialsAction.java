/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.spnego.web.flow;

import jcifs.util.Base64;
import org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials;
import org.jasig.cas.adaptors.spnego.util.SpnegoConstants;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Second action of a SPNEGO flow : decode the gssapi-data and build a new
 * {@link org.jasig.cas.adaptors.spnego.authentication.principal.SpnegoCredentials}.<br/>
 * Once AbstractNonInteractiveCredentialsAction has executed the authentication
 * procedure, this action check wether a principal is present in Credentials and
 * add correspondings response headers.
 *
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @version $Id$
 * @see <a href='http://ietfreport.isoc.org/idref/rfc4559/#page-2'>RFC 4559</a>
 * @since 3.1
 */
public class SpnegoCredentialsAction extends
        AbstractNonInteractiveCredentialsAction {

    protected Credentials constructCredentialsFromRequest(RequestContext context) {

        final HttpServletRequest request = WebUtils
                .getHttpServletRequest(context);

        final String authorizationHeader = request
                .getHeader(SpnegoConstants.HEADER_AUTHORIZATION);

        if (StringUtils.hasText(authorizationHeader)
                && authorizationHeader.startsWith(SpnegoConstants.NEGOTIATE
                + " ") && authorizationHeader.length() > 10) {
            if (logger.isDebugEnabled()) {
                logger.debug("SPNEGO Authorization header found with "
                        + (authorizationHeader.length() - 10) + " bytes");
            }
            final byte[] token = Base64.decode(authorizationHeader
                    .substring(10));
            if (logger.isDebugEnabled()) {
                logger.debug("Obtained token: " + new String(token));
            }
            return new SpnegoCredentials(token);
        } else {
            return null;
        }
    }

    protected void onError(RequestContext context, Credentials credentials) {
        setResponseHeader(context, credentials);
    }

    protected void onSuccess(RequestContext context, Credentials credentials) {
        setResponseHeader(context, credentials);
    }

    private void setResponseHeader(RequestContext context,
                                   Credentials credentials) {
        if (credentials != null) {
            final HttpServletResponse response = WebUtils
                    .getHttpServletResponse(context);
            SpnegoCredentials spnegoCredentials = (SpnegoCredentials) credentials;
            final byte[] nextToken = spnegoCredentials.getNextToken();
            if (nextToken != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Obtained output token: "
                            + new String(nextToken));
                }
                response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE,
                        SpnegoConstants.NEGOTIATE + " "
                                + Base64.encode(nextToken));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Unable to obtain the output token required.");
                }
            }
            if (spnegoCredentials.getPrincipal() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting HTTP Status to 401");
                }
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } else {
            logger.debug("Credentials are null");
        }
    }
}