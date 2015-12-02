package org.jasig.cas.web.flow;

import org.springframework.stereotype.Component;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;

/**
 * The {@link AcceptableUsagePolicyWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for aup integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("acceptableUsagePolicyWebflowConfigurer")
public class AcceptableUsagePolicyWebflowConfigurer extends AbstractCasWebflowConfigurer {

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        final ActionState actionState = createActionState(flow, "acceptableUsagePolicyCheck",
                createEvaluateAction("acceptableUsagePolicyFormAction.verify(flowRequestContext, flowScope.credential, messageContext)"));
        actionState.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        createStateDefaultTransition(actionState, "acceptableUsagePolicyView");


        final ViewState viewState = createViewState(flow, "acceptableUsagePolicyView", "casAcceptableUsagePolicyView");
        createTransitionForState(viewState, TRANSITION_ID_SUBMIT, "aupAcceptedAction");
        createStateDefaultTransition(actionState, STATE_ID_GENERATE_LOGIN_TICKET);

        final ActionState aupAcceptedAction = createActionState(flow, "aupAcceptedAction",
                createEvaluateAction("acceptableUsagePolicyFormAction.submit(flowRequestContext, flowScope.credential, messageContext)"));
        aupAcceptedAction.getTransitionSet().add(createTransition(TRANSITION_ID_SUCCESS, TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        aupAcceptedAction.getTransitionSet().add(createTransition(TRANSITION_ID_ERROR, getStartState(flow).getId()));
    }
}
