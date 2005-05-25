/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.Iterator;
import java.util.Map;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.action.AbstractAction;

public abstract class AbstractCasAction extends AbstractAction {

    protected final Event doExecute(final RequestContext requestContext)
        throws Exception {
        final Map attributes = requestContext.getFlowScope().getAttributeMap();
    
        final ModelAndEvent modelAndEvent = doExecuteInternal(requestContext, attributes);
        
        if (modelAndEvent == null) {
            throw new IllegalStateException("modelAndEvent not found.");
        }
        
        final Map model = modelAndEvent.getModel();
        
        for (Iterator iter = model.keySet().iterator(); iter.hasNext();) {
            final String key = (String) iter.next();
            ContextUtils.addAttributeToFlowScope(requestContext, key, model.get(key));
        }
        
        return modelAndEvent.getEvent();
    }

    protected abstract ModelAndEvent doExecuteInternal(
        final RequestContext requestContext, final Map attributes) throws Exception;
}
