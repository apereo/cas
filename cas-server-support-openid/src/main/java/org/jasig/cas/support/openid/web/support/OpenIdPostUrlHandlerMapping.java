/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

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
    protected Object lookupHandler(final String urlPath, final HttpServletRequest request) throws Exception {

        if ("POST".equals(request.getMethod()) && "check_authentication".equals(request.getParameter("openid.mode"))) {
            return super.lookupHandler(urlPath, request);
        }
        
        return null;
    }
}
