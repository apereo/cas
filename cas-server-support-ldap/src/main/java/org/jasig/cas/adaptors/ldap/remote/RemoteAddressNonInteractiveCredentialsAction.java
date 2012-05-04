/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.adaptors.ldap.remote;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RemoteAddressNonInteractiveCredentialsAction extends
    AbstractNonInteractiveCredentialsAction {

    protected Credentials constructCredentialsFromRequest(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final String remoteAddress = request.getRemoteAddr();
        
        if (StringUtils.hasText(remoteAddress)) {
            return new RemoteAddressCredentials(remoteAddress);
        }
        
        logger.debug("No remote address found.");
        return null;    
    }
}
