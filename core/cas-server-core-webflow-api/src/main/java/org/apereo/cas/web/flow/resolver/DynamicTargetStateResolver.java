package org.apereo.cas.web.flow.resolver;

import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.State;
import org.springframework.webflow.engine.TargetStateResolver;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DynamicTargetStateResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class DynamicTargetStateResolver implements TargetStateResolver {
    private final Flow flow;

    @Override
    public State resolveTargetState(final Transition transition, final State sourceState, final RequestContext context) {
        val targetState = WebUtils.getTargetState(context);
        return flow.getStateInstance(targetState);
    }
}
