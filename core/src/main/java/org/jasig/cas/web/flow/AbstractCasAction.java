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

/**
 * Abstract class that provides a convenient wrapper around the doExecute method
 * such that anything one places in the Model will be stored in the FlowScope to
 * later be accessed by any action that needs it.
 * <p>
 * It also exposes any of the attributes in the flowscope in a Map to the
 * doExecuteInternal method.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public abstract class AbstractCasAction extends AbstractAction {

    /**
     * doExecute executes a well-defined workflow of retrieving attributes from
     * the flowscope, executing doExecuteInternal and then repopulating the
     * flowscope with the model.
     */
    protected final Event doExecute(final RequestContext requestContext)
        throws Exception {
        final Map attributes = requestContext.getFlowScope().getAttributeMap();

        final ModelAndEvent modelAndEvent = doExecuteInternal(requestContext,
            attributes);
        
        final Map model = modelAndEvent.getModel();
        
        for (Iterator iter = model.keySet().iterator(); iter.hasNext();) {
            Object key = iter.next();
            ContextUtils.addAttributeToFlowScope(requestContext, (String) key, model.get(key));
        }

        return modelAndEvent.getEvent();
    }

    /**
     * Abstract method that gets executed by doExecute as part of the workflow.
     * 
     * @param requestContext the request context for the flow
     * @param attributes the map of FlowScope-ed attributes
     * @return the ModelAndEvent represending the request.
     * @throws Exception if there is an error.
     */
    protected abstract ModelAndEvent doExecuteInternal(
        final RequestContext requestContext, final Map attributes)
        throws Exception;
}
