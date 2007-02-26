/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

/**
 * Marker class to represent that this service wants to use SAML. We use this in
 * combination with the CentralAuthenticationServiceImpl to choose the right
 * UniqueTicketIdGenerator.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public final class SamlService extends WebApplicationService {

    /**
     * Unique Id for serialization.
     */
    private static final long serialVersionUID = -6867572626767140223L;

    
    protected SamlService(final String id) {
        super(id);
    }
    
    public static Service createServiceFrom(final HttpServletRequest request) {
        final String service = request.getParameter("TARGET");

        if (!StringUtils.hasText(service)) {
            return null;
        }
        
        final String id = cleanupUrl(service);
        
        return new SamlService(id);
    }
}
