package org.apereo.cas.interrupt.webflow.actions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareInterruptViewAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class PrepareInterruptViewAction extends AbstractAction {

    
    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return null;
    }
}
