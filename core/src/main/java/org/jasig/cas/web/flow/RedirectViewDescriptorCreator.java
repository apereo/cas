/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.ViewDescriptorCreator;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class RedirectViewDescriptorCreator implements
    ViewDescriptorCreator {

    public ViewDescriptor createViewDescriptor(
        final RequestContext requestContext) {
        final String service = (String) ContextUtils.getAttributeFromFlowScope(
            requestContext, WebConstants.SERVICE);
        final String ticket = (String) ContextUtils.getAttributeFromFlowScope(
            requestContext, WebConstants.TICKET);

        ViewDescriptor descriptor = new ViewDescriptor(service,
            WebConstants.TICKET, ticket);
        descriptor.setRedirect(true);

        return descriptor;
    }
}
