/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public final class OpenIdPostUrlHandlerMapping extends SimpleUrlHandlerMapping {

    @Override
    protected Object lookupHandler(final String urlPath, final HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Request Method Type: " + request.getMethod());
            logger.debug("Request Parameter: " + request.getParameter("openid.mode"));
        }
        if ("POST".equals(request.getMethod()) && "check_authentication".equals(request.getParameter("openid.mode"))) {
            logger.debug("Using this Handler.");
            return super.lookupHandler(urlPath, request);
        }
        
        logger.debug("Delegating to next handler.");
        
        return null;
    }
}
