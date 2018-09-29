package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.TransitionSet;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * The {@link AcceptableUsagePolicyWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for aup integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Slf4j
public class AcceptableUsagePolicyWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private static final String VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW = "acceptableUsagePolicyView";
    private static final String AUP_ACCEPTED_ACTION = "aupAcceptedAction";
    private static final String STATE_ID_AUP_CHECK = "acceptableUsagePolicyCheck";

    public AcceptableUsagePolicyWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                  final ApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();

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
        final ActionState submit = getRealSubmissionState(flow);
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
        final ActionState aupAcceptedAction = createActionState(flow, AUP_ACCEPTED_ACTION, "acceptableUsagePolicySubmitAction");

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        final TransitionSet transitionSet = aupAcceptedAction.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    /**
     * Create acceptable usage policy view.
     *
     * @param flow the flow
     */
    protected void createAcceptableUsagePolicyView(final Flow flow) {
        final ViewState viewState = createViewState(flow, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW, "casAcceptableUsagePolicyView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, AUP_ACCEPTED_ACTION);
    }

    /**
     * Create verify action state.
     *
     * @param flow the flow
     */
    protected void createVerifyActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, STATE_ID_AUP_CHECK, "acceptableUsagePolicyVerifyAction");
        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        final TransitionSet transitionSet = actionState.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        transitionSet.add(createTransition(AcceptableUsagePolicyVerifyAction.EVENT_ID_MUST_ACCEPT, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW));

        final ActionState ticketCreateState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
        prependActionsToActionStateExecutionList(flow, ticketCreateState, "acceptableUsagePolicyVerifyAction");
        createTransitionForState(ticketCreateState, AcceptableUsagePolicyVerifyAction.EVENT_ID_MUST_ACCEPT, VIEW_ID_ACCEPTABLE_USAGE_POLICY_VIEW);
    }
}
