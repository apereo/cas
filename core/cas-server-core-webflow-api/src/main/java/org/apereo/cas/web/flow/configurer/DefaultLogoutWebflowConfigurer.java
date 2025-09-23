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

    public DefaultLogoutWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                          final FlowDefinitionRegistry flowDefinitionRegistry,
                                          final ConfigurableApplicationContext applicationContext,
                                          final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    private void configureFlowStartState(final Flow flow) {
        val startState = getState(flow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION, ActionState.class);
        LOGGER.trace("Setting the start state of the logout webflow identified by [{}] to [{}]", flow.getId(), startState.getId());
        flow.setStartState(startState);
    }

    @Override
    protected void doInitialize() {
        val flow = getLogoutFlow();

        if (flow != null) {
            configureStartActions(flow);
            createTerminateSessionActionState(flow);
            createLogoutConfirmationView(flow);
            createDoLogoutActionState(flow);
            createFrontLogoutActionState(flow);
            createLogoutPropagationEndState(flow);
            createLogoutViewState(flow);
            createFinishLogoutState(flow);
            configureFlowStartState(flow);
        }
    }

    private void configureStartActions(final Flow flow) {
        val startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
    }

    protected void createTerminateSessionActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION,
            CasWebflowConstants.ACTION_ID_TERMINATE_SESSION);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_WARN,
            CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW);
        createStateDefaultTransition(actionState, CasWebflowConstants.STATE_ID_DO_LOGOUT);
    }

    protected void createFinishLogoutState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, CasWebflowConstants.ACTION_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT_VIEW);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_POST, CasWebflowConstants.STATE_ID_POST_VIEW);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_LOGOUT_VIEW);
    }

    protected void createLogoutViewState(final Flow flow) {
        createEndViewState(flow, CasWebflowConstants.STATE_ID_REDIRECT_VIEW, createExternalRedirectViewFactory("flowScope.logoutRedirectUrl"));
        createEndViewState(flow, CasWebflowConstants.STATE_ID_POST_VIEW, CasWebflowConstants.VIEW_ID_POST_RESPONSE);

        val logoutView = createViewState(flow, CasWebflowConstants.STATE_ID_LOGOUT_VIEW, "logout/casLogoutView");
        logoutView.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP));
        createTransitionForState(logoutView, CasWebflowConstants.TRANSITION_ID_PROCEED, "proceedFromLogout");
        createEndViewState(flow, "proceedFromLogout", CasWebflowConstants.VIEW_ID_DYNAMIC_HTML);
    }

    protected void createFrontLogoutActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_FRONT_LOGOUT,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_FRONT_CHANNEL_LOGOUT));
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH,
            CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_PROPAGATE,
            CasWebflowConstants.STATE_ID_PROPAGATE_LOGOUT_REQUESTS);
    }

    protected void createLogoutConfirmationView(final Flow flow) {
        val view = createViewState(flow, CasWebflowConstants.STATE_ID_CONFIRM_LOGOUT_VIEW, "logout/casConfirmLogoutView");
        view.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CONFIRM_LOGOUT));
        createTransitionForState(view, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
    }

    private void createLogoutPropagationEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_PROPAGATE_LOGOUT_REQUESTS, "logout/casPropagateLogoutView");
    }

    private void createDoLogoutActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_DO_LOGOUT, CasWebflowConstants.ACTION_ID_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FINISH, CasWebflowConstants.STATE_ID_FINISH_LOGOUT);
        createTransitionForState(actionState, CasWebflowConstants.TRANSITION_ID_FRONT, CasWebflowConstants.STATE_ID_FRONT_LOGOUT);
    }

}

