package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link StaticEventExecutionAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
public class StaticEventExecutionAction extends BaseCasWebflowAction {
    /**
     * Null/NoOp action.
     */
    public static final Action NULL = new StaticEventExecutionAction(null);

    /**
     * Success action.
     */
    public static final Action SUCCESS = new StaticEventExecutionAction(CasWebflowConstants.TRANSITION_ID_SUCCESS);

    private final String eventId;

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        return Optional.ofNullable(eventId)
            .map(id -> eventFactory.event(this, id))
            .orElse(null);
    }
}
