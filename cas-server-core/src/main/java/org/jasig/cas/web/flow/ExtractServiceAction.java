/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Extracts the service from the request and places it in the flow.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class ExtractServiceAction extends AbstractAction {

    /** Extractors for finding the service. */
    private ArgumentExtractor[] argumentExtractors;
    
    protected Event doExecute(final RequestContext context) throws Exception {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final Service service = WebUtils.getService(this.argumentExtractors, request);
        
        if (service != null && logger.isDebugEnabled()) {
            logger.debug("Placing service in FlowScope: " + service.getId());
        }
        
        context.getFlowScope().put("service", service);
        
        return success();   
    }

    public void setArgumentExtractors(final ArgumentExtractor[] argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
    } 
}
