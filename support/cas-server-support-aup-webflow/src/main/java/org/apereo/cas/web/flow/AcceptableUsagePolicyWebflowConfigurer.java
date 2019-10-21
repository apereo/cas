package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link AcceptableUsagePolicyWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for aup integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class AcceptableUsagePolicyWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW = "acceptableUsagePolicyView";

    private static final String AUP_ACCEPTED_ACTION = "aupAcceptedAction";
    private static final String AUP_VERIFY_ACTION = "acceptableUsagePolicyVerifyAction";
    private static final String AUP_VERIFY_SERVICE_ACTION = "acceptableUsagePolicyVerifyServiceAction";

    private static final String STATE_ID_AUP_CHECK = "acceptableUsagePolicyCheck";

    public AcceptableUsagePolicyWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                  final ApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createVerifyActionState(flow);
            createAcceptableUsagePolicyView(flow);
            createSubmitActionState(flow);
            createTransitionStateToAcceptableUsagePolicy(flow);
        }
    }

    /**
     * Create transition state to acceptable usage policy.
     *
     * @param flow the flow
     */
    protected void createTransitionStateToAcceptableUsagePolicy(final Flow flow) {
        val submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_AUP_CHECK, true);
    }

    /**
     * Gets real submission state.
     *
     * @param flow the flow
     * @return the real submission state
     */
    protected ActionState getRealSubmissionState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    /**
     * Create submit action state.
     *
     * @param flow the flow
     */
    protected void createSubmitActionState(final Flow flow) {
        val aupAcceptedAction = createActionState(flow, AUP_ACCEPTED_ACTION, "acceptableUsagePolicySubmitAction");
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        val transitionSet = aupAcceptedAction.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    /**
     * Create acceptable usage policy view.
     *
     * @param flow the flow
     */
    protected void createAcceptableUsagePolicyView(final Flow flow) {
        val viewState = createViewState(flow, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW, "casAcceptableUsagePolicyView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, AUP_ACCEPTED_ACTION);
    }

    /**
     * Create verify action state.
     *
     * @param flow the flow
     */
    protected void createVerifyActionState(final Flow flow) {
        val actionState = createActionState(flow, STATE_ID_AUP_CHECK, AUP_VERIFY_ACTION);

        val transitionSet = actionState.getTransitionSet();
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW));

        val ticketCreateState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
        createEvaluateActionForExistingActionState(flow, ticketCreateState.getId(), AUP_VERIFY_ACTION);
        createTransitionForState(ticketCreateState, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW);
        
        val genServiceTicketState = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        createEvaluateActionForExistingActionState(flow, genServiceTicketState.getId(), AUP_VERIFY_SERVICE_ACTION);
        createTransitionForState(genServiceTicketState, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW);
    }
}
