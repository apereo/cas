/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.windows.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.support.windows.util.SpnegoConstants;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * First action of a SPNEGO flow : negociation.<br/> The server checks if the
 * negociation string is in the request header:
 * <ul>
 * <li>If found do nothing and return <code>success()</code></li>
 * <li>else add a WWW-Authenticate response header and a 401 response status,
 * then return <code>success()</code></li>
 * </ul>
 * 
 * @see <a href='http://ietfreport.isoc.org/idref/rfc4559/#page-2'>RFC 4559</a>
 * @author Arnaud Lesueur
 * @author Marc-Antoine Garrigue
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SpnegoNegociateCredentialsAction extends AbstractAction {

    /** Whether this is using the NTLM protocol or not. */
    private boolean ntlm = false;
    
    private String messageBeginPrefix = constructMessagePrefix();

    protected Event doExecute(RequestContext context) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        final HttpServletResponse response = WebUtils
            .getHttpServletResponse(context);
        final String authorizationHeader = request
            .getHeader(SpnegoConstants.HEADER_AUTHORIZATION);

        if (!StringUtils.hasText(authorizationHeader)
            || !authorizationHeader.startsWith(this.messageBeginPrefix)
            || authorizationHeader.length() <= this.messageBeginPrefix.length()) {
            if (logger.isDebugEnabled()) {
                logger
                    .debug("Authorization header not found.  Sending WWW-Authenticate header");
            }
            response.setHeader(SpnegoConstants.HEADER_AUTHENTICATE, this.ntlm
                ? SpnegoConstants.NTLM : SpnegoConstants.NEGOTIATE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return success();
    }

    public void setNtlm(final boolean ntlm) {
        this.ntlm = ntlm;
        this.messageBeginPrefix = constructMessagePrefix();
    }
    
    protected String constructMessagePrefix() {
        return (this.ntlm ? SpnegoConstants.NTLM
            : SpnegoConstants.NEGOTIATE)
            + " ";
    }
}
