package org.apereo.cas.web.flow.resolver;

import org.apereo.cas.web.flow.CasWebflowConstants;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link CurrentEventViewTargetStateResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class CurrentEventViewTargetStateResolver implements TargetStateResolver {
    private final Flow flow;

    @Override
    public State resolveTargetState(final Transition transition, final State state, final RequestContext requestContext) {
        val viewStateId = (String) requestContext.getCurrentEvent().getAttributes().get(CasWebflowConstants.ATTRIBUTE_CURRENT_EVENT_VIEW);
        return flow.getStateInstance(viewStateId);
    }
}
