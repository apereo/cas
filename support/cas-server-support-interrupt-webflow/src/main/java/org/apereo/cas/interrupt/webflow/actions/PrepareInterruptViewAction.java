package org.apereo.cas.interrupt.webflow.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareInterruptViewAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class PrepareInterruptViewAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrepareInterruptViewAction.class);
    
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return null;
    }
}
