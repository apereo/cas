package org.apereo.cas.web.flow;

import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
/**
 * This is {@link BasicAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class BasicAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, "basicAuthenticationCheck",
                createEvaluateAction("basicAuthenticationAction"));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.TRANSITION_ID_WARN));
        actionState.getExitActionList().add(createEvaluateAction("clearWebflowCredentialsAction"));
        registerMultifactorProvidersStateTransitionsIntoWebflow(actionState);
        
        createStateDefaultTransition(actionState, getStartState(flow).getId());
        setStartState(flow, actionState);
    }
}
