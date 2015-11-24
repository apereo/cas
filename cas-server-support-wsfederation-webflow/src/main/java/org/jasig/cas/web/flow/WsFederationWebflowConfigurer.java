package org.jasig.cas.web.flow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;

/**
 * The {@link WsFederationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for wsfed integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("wsFederationWebflowConfigurer")
public class WsFederationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        addViewState(flow, "wsFederationRedirect", "externalRedirect:#{flowScope.WsFederationIdentityProviderUrl}");
        final ActionState actionState = createActionState(flow, "wsFederationAction", createEvaluateAction("wsFederationAction"));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_ERROR, getStartState(flow).getId()));
        setStartState(flow, actionState);
    }
}
