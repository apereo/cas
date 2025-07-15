package org.apereo.cas.web.flow.states;

import org.apereo.cas.util.function.FunctionUtils;
import lombok.Setter;
import lombok.val;
import org.springframework.util.ReflectionUtils;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.RequestControlContext;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.FlowExecutionException;
import org.springframework.webflow.execution.RequestContext;
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
            if (shouldRenderView(context)) {
                val view = getViewFactory().getView(context);
                context.setCurrentView(view);
                context.viewRendering(view);
                getRenderActionList().execute(context);
                FunctionUtils.doAndHandle(__ -> view.render());
                context.getFlashScope().clear();
                context.getMessageContext().clearMessages();
                context.getExternalContext().recordResponseComplete();
                context.viewRendered(view);
            }
        } finally {
            context.endActiveFlowSession(this.getId(), new LocalAttributeMap<>());
        }
    }

    /**
     * Decide if the view should be forcefully rendered.
     * The view will be rendered if the view is not an
     * {@link ExternalRedirectAction}, meaning we are not set
     * to redirect to an external url after the state is done.
     *
     * @param context the context
     * @return true/false
     */
    protected boolean shouldRenderView(final RequestContext context) {
        if (forceRenderView) {
            val field = ReflectionUtils.findField(context.getCurrentView().getClass(), "action");
            if (field != null) {
                field.trySetAccessible();
                val action = (Action) ReflectionUtils.getField(field, context.getCurrentView());
                return !(action instanceof ExternalRedirectAction);
            }
            return true;
        }
        return false;
    }
}
