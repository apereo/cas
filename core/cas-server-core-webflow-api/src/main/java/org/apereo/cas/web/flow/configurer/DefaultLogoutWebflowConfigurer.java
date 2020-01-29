package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;

/**
 * This is {@link DefaultLogoutWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class DefaultLogoutWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public DefaultLogoutWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                          final FlowDefinitionRegistry flowDefinitionRegistry,
                                          final ConfigurableApplicationContext applicationContext,
                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLogoutFlow();

        if (flow != null) {
            val terminateSessionActionState = createTerminateSessionActionState(flow);
            createLogoutConfirmationView(flow);
            createDoLogoutActionState(flow);
            createFrontLogoutActionState(flow);
            createLogoutPropagationEndState(flow);
            createLogoutViewState(flow);
            createFinishLogoutDecisionState(flow);
            configureFlowStartState(flow, terminateSessionActionState);
        }
    }

    private static void configureFlowStartState(final Flow flow, final ActionState terminateSessionActionState) {
        LOGGER.trace("Setting the start state of the logout webflow identified by [{}] to [{}]", flow.getId(), terminateSessionActionState.getId());
        flow.setStartState(terminateSessionActionState);
    }

    /**
     * Create terminate session action state.
     *
     * @param flow the flow
     * @return the action state
     */
    protected ActionState createTerminateSessionActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION, CasWebflowConstants.ACTION_ID_TERMINATE_SESSION);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW);
        createStateDefaultTransition(actionState, CasWebflowConstants.STATE_ID_DO_LOGOUT);
        return actionState;
    }

    /**
     * Create finish logout decision state.
     *
     * @param flow the flow
     */
    protected void createFinishLogoutDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.DECISION_STATE_FINISH_LOGOUT, "flowScope.logoutRedirectUrl != null",
            "redirectView", CasWebflowConstants.STATE_ID_LOGOUT_VIEW);
    }

    /**
     * Create logout view state.
     *
     * @param flow the flow
     */
    protected void createLogoutViewState(final Flow flow) {
        val logoutView = createEndState(flow, CasWebflowConstants.STATE_ID_LOGOUT_VIEW, "casLogoutView");
        logoutView.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP));
    }

    /**
     * Create logout propagation end state.
     *
     * @param flow the flow
     */
    private void createLogoutPropagationEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_PROPAGATE_LOGOUT_REQUESTS, "casPropagateLogoutView");
    }

    /**
     * Create front logout action state.
     *
     * @param flow the flow
     */
    protected void createFrontLogoutActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_FRONT_LOGOUT,
            createEvaluateAction("frontChannelLogoutAction"));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_PROPAGATE,
            CasWebflowConstants.STATE_ID_PROPAGATE_LOGOUT_REQUESTS);
    }

    /**
     * Create do logout action state.
     *
     * @param flow the flow
     */
    private void createDoLogoutActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_DO_LOGOUT, createEvaluateAction("logoutAction"));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, "front", CasWebflowConstants.STATE_ID_FRONT_LOGOUT);
    }

    /**
     * Create logout confirmation view.
     *
     * @param flow the flow
     */
    protected void createLogoutConfirmationView(final Flow flow) {
        val view = createViewState(flow, CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW, "casConfirmLogoutView");
        createTransitionForState(view, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
    }

}

