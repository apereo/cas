package org.apereo.cas.web.flow;

import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;

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
        createEndState(flow, WS_FEDERATION_REDIRECT, "flowScope.WsFederationIdentityProviderUrl", true);
        final ActionState actionState = createActionState(flow, WS_FEDERATION_ACTION, createEvaluateAction(WS_FEDERATION_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, WS_FEDERATION_REDIRECT));
        
        if (this.autoRedirect) {
            setStartState(flow, actionState);
        }
    }

    public void setAutoRedirect(final boolean autoRedirect) {
        this.autoRedirect = autoRedirect;
    }
}
