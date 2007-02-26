/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.authentication.principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public class WebApplicationService implements Service {

    /**
     * Unique Id for Serialization
     */
    private static final long serialVersionUID = 8334068957483758042L;
    
    private final String id;
    
    protected WebApplicationService(final String id) {
        this.id = id;
    }

    public final String getId() {
        return this.id;
    }
    
    public static Service createServiceFrom(final HttpServletRequest request) {
        final String service = request.getParameter("service");
        
        return createServiceFrom(service);
    }
    
    public static Service createServiceFrom(final String service) {
        if (!StringUtils.hasText(service)) {
            return null;
        }
        
        final String id = cleanupUrl(service);
            
        return new WebApplicationService(id);
    }
    
    protected static final String cleanupUrl(final String url) {
        final int jsessionPosition = url.indexOf(";jsession");

        if (jsessionPosition == -1) {
            return url;
        }

        final int questionMarkPosition = url.indexOf("?");

        if (questionMarkPosition < jsessionPosition) {
            return url.substring(0, url.indexOf(";jsession"));
        }

        return url.substring(0, jsessionPosition)
            + url.substring(questionMarkPosition);
    }
    
    public final boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        
        if (object instanceof Service) {
            final Service service = (Service) object;
            
            return this.id.equals(service.getId());
        }
        
        return false;
    }
}
