/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SimpleService;
import org.springframework.util.StringUtils;
import org.springframework.webflow.RequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    public final Service extractService(final RequestContext context) {
        final String service = context.getRequestParameters().get(getServiceParameterName());
        
        if (!StringUtils.hasText(service)) {
            return null;
        }
        
        return new SimpleService(WebUtils.stripJsessionFromUrl(service));
    }

    public final String extractTicketArtifact(final RequestContext context) {
        return context.getRequestParameters().get(getArtifactParameterName());
    }

    public final String extractTicketArtifact(final HttpServletRequest request) {
        return request.getParameter(getArtifactParameterName());
    }
    
    public final Service extractService(final HttpServletRequest request) {
        final String service = request.getParameter(getServiceParameterName());
        
        if (!StringUtils.hasText(service)) {
            return null;
        }
        
        return new SimpleService(WebUtils.stripJsessionFromUrl(service));
    }
    
    protected abstract String getServiceParameterName();
    
    protected abstract String getArtifactParameterName();
}
