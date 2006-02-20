/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

/**
 * Web flow action that 
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public final class SpnegoHttpProtocolAction extends AbstractAction {

    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = ContextUtils.getHttpServletRequest(context);
        final HttpServletResponse response = ContextUtils.getHttpServletResponse(context);
        
        if (request.getHeader("WWW-Authenticate") == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("WWW-Authenticate header not found.  Sending 401 and WWW-Authenticate: Negotiate");
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Negotiate");
            return error();
        }
        
        if (logger.isDebugEnabled()) {
            logger.debug("WWW-Authenticate header found");
        }
        
        return success();
    }
}
