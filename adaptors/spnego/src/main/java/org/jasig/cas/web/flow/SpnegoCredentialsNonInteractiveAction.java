/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SpnegoCredentials;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.webflow.RequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public class SpnegoCredentialsNonInteractiveAction extends
    AbstractNonInteractiveCredentialsAction {

    protected Credentials constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = ContextUtils.getHttpServletRequest(context);
        
        final String wwwAuthenticate = request.getHeader("WWW-Authenticate");
        
        if (wwwAuthenticate == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("WWW-Authenticate header not found in request.");
            }
            return null;
        }

        final String spnegoToken = wwwAuthenticate.substring(wwwAuthenticate.indexOf(" ")+1);
        
        if (logger.isDebugEnabled()) {
            logger.debug("WWW-Authenticate header found with the following token: " + spnegoToken);
        }

        return new SpnegoCredentials(spnegoToken);
    }
}
