package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
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

    public AcceptableUsagePolicyWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                  final FlowDefinitionRegistry flowDefinitionRegistry,
                                                  final ConfigurableApplicationContext applicationContext,
                                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
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

    protected void createTransitionStateToAcceptableUsagePolicy(final Flow flow) {
        val submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_AUP_CHECK, true);
    }

    protected ActionState getRealSubmissionState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    /**
     * Create submit action state.
     *
     * @param flow the flow
     */
    protected void createSubmitActionState(final Flow flow) {
        val aupAcceptedAction = createActionState(flow, CasWebflowConstants.STATE_ID_AUP_ACCEPTED, CasWebflowConstants.ACTION_ID_AUP_SUBMIT);
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        val transitionSet = aupAcceptedAction.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    protected void createAcceptableUsagePolicyView(final Flow flow) {
        val viewState = createViewState(flow, CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW, "aup/casAcceptableUsagePolicyView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_AUP_ACCEPTED);
        viewState.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_AUP_RENDER));
    }

    protected void createVerifyActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_AUP_CHECK, CasWebflowConstants.ACTION_ID_AUP_VERIFY);

        val transitionSet = actionState.getTransitionSet();
        val target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();

        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_ACCEPTED, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SKIP, target));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT,
            CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW));

        val ticketCreateState = getState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, ActionState.class);
        createEvaluateActionForExistingActionState(flow, ticketCreateState.getId(), CasWebflowConstants.ACTION_ID_AUP_VERIFY);
        createTransitionForState(ticketCreateState, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT,
            CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW);

        val genServiceTicketState = getState(flow, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET, ActionState.class);
        createEvaluateActionForExistingActionState(flow, genServiceTicketState.getId(),
            CasWebflowConstants.ACTION_ID_AUP_VERIFY_SERVICE);
        createTransitionForState(genServiceTicketState, CasWebflowConstants.TRANSITION_ID_AUP_MUST_ACCEPT,
            CasWebflowConstants.STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW);
    }
}
