package org.apereo.cas.web.flow;

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
public class AcceptableUsagePolicyWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String ACCEPTABLE_USAGE_POLICY_VIEW = "acceptableUsagePolicyView";
    private static final String AUP_ACCEPTED_ACTION = "aupAcceptedAction";

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        final ActionState actionState = createActionState(flow, "acceptableUsagePolicyCheck",
                createEvaluateAction("acceptableUsagePolicyFormAction.verify(flowRequestContext, flowScope.credential, messageContext)"));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        createStateDefaultTransition(actionState, ACCEPTABLE_USAGE_POLICY_VIEW);


        final ViewState viewState = createViewState(flow, ACCEPTABLE_USAGE_POLICY_VIEW, "casAcceptableUsagePolicyView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, AUP_ACCEPTED_ACTION);
        createStateDefaultTransition(actionState, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

        final ActionState aupAcceptedAction = createActionState(flow, AUP_ACCEPTED_ACTION,
                createEvaluateAction("acceptableUsagePolicyFormAction.submit(flowRequestContext, flowScope.credential, messageContext)"));
        aupAcceptedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.TRANSITION_ID_SEND_TICKET_GRANTING_TICKET));
        aupAcceptedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, 
                CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }
}
