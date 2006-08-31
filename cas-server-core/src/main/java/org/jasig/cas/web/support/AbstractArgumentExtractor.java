/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.support;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.webflow.RequestContext;

/**
 * Abstract class providing common functionality for extracting arguments.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractArgumentExtractor implements ArgumentExtractor {

    public final String extractTicketArtifact(final RequestContext context) {
        return context.getRequestParameters().get(getArtifactParameterName());
    }

    public final Service extractService(RequestContext context) {
        return extractService(WebUtils.getHttpServletRequest(context));
    }

    public final String extractTicketArtifact(final HttpServletRequest request) {
        return request.getParameter(getArtifactParameterName());
    }

    /**
     * Retrieve the parameter used to represent the service.
     * 
     * @return the parameter representing the service.
     */
    protected abstract String getServiceParameterName();

    /**
     * Retrieve the parameter used to represent the artifact.
     * 
     * @return the parameter representing the artifact.
     */
    protected abstract String getArtifactParameterName();
}
