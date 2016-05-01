package org.jasig.cas.web.flow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;

/**
 * The {@link Pac4jWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("pac4jWebflowConfigurer")
public class Pac4jWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String CLIENT_ACTION = "clientAction";
    private static final String STOP_WEBFLOW = "stopWebflow";

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, CLIENT_ACTION, createEvaluateAction(CLIENT_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition("stop", STOP_WEBFLOW));
        setStartState(flow, actionState);
        createViewState(flow, STOP_WEBFLOW, STOP_WEBFLOW);
    }
}
