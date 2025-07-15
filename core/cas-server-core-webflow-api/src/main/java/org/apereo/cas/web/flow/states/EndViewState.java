package org.apereo.cas.web.flow.states;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.Setter;
import lombok.val;
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
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Setter
public class EndViewState extends ViewState {
    private boolean forceRenderView;

    public EndViewState(final Flow flow, final String id,
                        final ViewFactory viewFactory) {
        super(flow, id, viewFactory);
    }


    @Override
    protected void doEnter(final RequestControlContext context) throws FlowExecutionException {
        try {
            super.doEnter(context);
            if (forceRenderView) {
                val view = getViewFactory().getView(context);
                if (view != null) {
                    context.setCurrentView(view);
                    context.viewRendering(view);
                    getRenderActionList().execute(context);
                    FunctionUtils.doAndHandle(__ -> view.render());
                    context.getFlashScope().clear();
                    context.getMessageContext().clearMessages();
                    context.getExternalContext().recordResponseComplete();
                    context.viewRendered(view);
                }
            }
        } finally {
            context.endActiveFlowSession(this.getId(), new LocalAttributeMap<>());
        }
    }
}
