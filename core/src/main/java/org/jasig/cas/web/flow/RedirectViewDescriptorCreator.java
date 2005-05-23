/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.ViewDescriptorCreator;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class RedirectViewDescriptorCreator implements ViewDescriptorCreator {

    public ViewDescriptor createViewDescriptor(RequestContext requestContext) {
        final String service = (String) ContextUtils.getAttributeFromFlowScope(requestContext, WebConstants.SERVICE);
        final String ticket = (String) ContextUtils.getAttributeFromFlowScope(requestContext, WebConstants.TICKET);
        final Map model = new HashMap();
        
        model.put(WebConstants.TICKET, ticket);
        
        ViewDescriptor descriptor = new ViewDescriptor(service, model);
        descriptor.setRedirect(true);
        
        return descriptor;
    }
}
