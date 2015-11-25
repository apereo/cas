package org.jasig.cas.web.flow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;

/**
 * The {@link OpenIdWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for openid integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("openidWebflowConfigurer")
public class OpenIdWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        final String condition = "externalContext.requestParameterMap['openid.mode'] ne '' "
                + "&& externalContext.requestParameterMap['openid.mode'] ne null "
                + "&& externalContext.requestParameterMap['openid.mode'] ne 'associate'";

        final DecisionState decisionState = createDecisionState(flow, "selectFirstAction",
                condition, "openIdSingleSignOnAction",
                getStartState(flow).getId());

        final ActionState actionState = createActionState(flow, "openIdSingleSignOnAction",
                createEvaluateAction("openIdSingleSignOnAction"));

        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_WARN, TRANSITION_ID_WARN));

        setStartState(flow, decisionState);
    }
}
