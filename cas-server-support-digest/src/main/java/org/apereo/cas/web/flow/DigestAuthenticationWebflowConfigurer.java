package org.apereo.cas.web.flow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
/**
 * This is {@link DigestAuthenticationWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("digestAuthenticationWebflowConfigurer")
public class DigestAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();
        final ActionState actionState = createActionState(flow, "digestAuthenticationCheck",
                createEvaluateAction("digestAuthenticationAction"));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN,
                CasWebflowConstants.TRANSITION_ID_WARN));
        createStateDefaultTransition(actionState, getStartState(flow).getId());
        setStartState(flow, actionState);
    }
}
