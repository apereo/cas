package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EvaluateAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

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

    private void createTransitionStateToAcceptableUsagePolicy(final Flow flow) {
        final ActionState submit = getRealSubmissionState(flow);
        createTransitionForState(submit, CasWebflowConstants.TRANSITION_ID_SUCCESS, STATE_ID_AUP_CHECK, true);
    }

    private ActionState getRealSubmissionState(final Flow flow) {
        return getState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, ActionState.class);
    }

    private EvaluateAction createAcceptableUsagePolicyAction(final String actionId) {
        return createEvaluateAction("acceptableUsagePolicyFormAction."
                + actionId + "(flowRequestContext, flowScope.credential, messageContext)");
    }

    private void createSubmitActionState(final Flow flow) {
        final ActionState aupAcceptedAction = createActionState(flow, AUP_ACCEPTED_ACTION, createAcceptableUsagePolicyAction("submit"));

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        aupAcceptedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        aupAcceptedAction.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM));
    }

    private void createAcceptableUsagePolicyView(final Flow flow) {
        final ViewState viewState = createViewState(flow, ACCEPTABLE_USAGE_POLICY_VIEW, "casAcceptableUsagePolicyView");
        createTransitionForState(viewState, CasWebflowConstants.TRANSITION_ID_SUBMIT, AUP_ACCEPTED_ACTION);
    }

    private void createVerifyActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, STATE_ID_AUP_CHECK, createAcceptableUsagePolicyAction("verify"));

        final String target = getRealSubmissionState(flow).getTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS).getTargetStateId();
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, target));
        actionState.getTransitionSet().add(createTransition(AcceptableUsagePolicyFormAction.EVENT_ID_MUST_ACCEPT, ACCEPTABLE_USAGE_POLICY_VIEW));
    }
}
