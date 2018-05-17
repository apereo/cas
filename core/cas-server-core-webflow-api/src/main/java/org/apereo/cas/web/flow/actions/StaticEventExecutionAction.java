package org.apereo.cas.web.flow.actions;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link StaticEventExecutionAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class StaticEventExecutionAction extends AbstractAction {
    private final String eventId;

    @Override
    protected Event doExecute(final RequestContext context) {
        return new EventFactorySupport().event(this, this.eventId);
    }
}
