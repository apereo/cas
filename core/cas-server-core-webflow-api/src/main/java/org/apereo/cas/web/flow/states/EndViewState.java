package org.apereo.cas.web.flow.states;

import lombok.Setter;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.ViewFactory;

/**
 * This is {@link EndViewState}.
 * This is a view state that also ends the view
 * regardless of the calling flow. The main difference
 * between this view and {@link org.springframework.webflow.engine.EndState}
 * is that this view does not consider the calling flow's root session
 * and will always end the view state. In other words, this view will always
 * end the flow session, whether called directly or as part of a subflow.
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Setter
public class EndViewState extends ViewState {
    public EndViewState(final Flow flow, final String id, final ViewFactory viewFactory) {
        super(flow, id, viewFactory);
    }

    @Override
    protected void doEnter(final RequestControlContext context) throws FlowExecutionException {
        try {
            super.doEnter(context);
        } finally {
            context.endActiveFlowSession(this.getId(), new LocalAttributeMap<>());
        }
    }
}
