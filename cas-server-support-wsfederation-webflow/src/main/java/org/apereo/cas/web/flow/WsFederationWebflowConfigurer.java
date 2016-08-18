package org.apereo.cas.web.flow;

import org.springframework.binding.expression.Expression;
import org.springframework.webflow.action.ExternalRedirectAction;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.support.ActionExecutingViewFactory;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String WS_FEDERATION_ACTION = "wsFederationAction";
    private static final String WS_FEDERATION_REDIRECT = "wsFederationRedirect";

    private boolean autoRedirect = true;
    
    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        
        final Expression expression = createExpression("flowScope.WsFederationIdentityProviderUrl", String.class);
        final ActionExecutingViewFactory viewFactory = new ActionExecutingViewFactory(
                new ExternalRedirectAction(expression));

        createEndState(flow, WS_FEDERATION_REDIRECT, viewFactory);
        final ActionState actionState = createActionState(flow, WS_FEDERATION_ACTION, createEvaluateAction(WS_FEDERATION_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, WS_FEDERATION_REDIRECT));
        
        if (this.autoRedirect) {
            setStartState(flow, actionState);
        }
    }

    public boolean isAutoRedirect() {
        return autoRedirect;
    }

    public void setAutoRedirect(final boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }
}
