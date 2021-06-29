package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * The {@link DelegatedAuthenticationWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class DelegatedAuthenticationWebflowConfigurer extends AbstractCasWebflowConfigurer {
    private static final String DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE = "checkDelegatedAuthnFailureDecision";

    public DelegatedAuthenticationWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                                    final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                                    final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                                    final ConfigurableApplicationContext applicationContext,
                                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        if (flow != null) {
            createClientActionState(flow);
            createStopWebflowViewState(flow);
            createDelegatedClientLogoutAction();
        }
    }

    /**
     * Create delegated client logout action.
     */
    protected void createDelegatedClientLogoutAction() {
        val logoutFlow = getLogoutFlow();

        val terminateSessionState = getState(logoutFlow, CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        terminateSessionState.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT));

        val finishLogout = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, ActionState.class);
        finishLogout.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT));
    }

    /**
     * Create client action action state.
     *
     * @param flow the flow
     */
    protected void createClientActionState(final Flow flow) {
        val actionState = createActionState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION));
        
        val transitionSet = actionState.getTransitionSet();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));

        val currentStartState = getStartState(flow).getId();
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, currentStartState));

        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_RESUME, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_STOP, CasWebflowConstants.STATE_ID_STOP_WEBFLOW));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN));
        transitionSet.add(createTransition(CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET));
        setStartState(flow, actionState);
    }

    /**
     * Create stop webflow view state.
     *
     * @param flow the flow
     */
    protected void createStopWebflowViewState(final Flow flow) {
        createDecisionState(flow, DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE, "flowScope.unauthorizedRedirectUrl != null",
            CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);

        val stopWebflowState = createViewState(flow, CasWebflowConstants.STATE_ID_STOP_WEBFLOW, CasWebflowConstants.VIEW_ID_PAC4J_STOP_WEBFLOW);
        stopWebflowState.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                val mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request, response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v)));
                return null;
            }
        });
        createTransitionForState(stopWebflowState, CasWebflowConstants.TRANSITION_ID_RETRY, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        val retryState = createEndState(flow, CasWebflowConstants.STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY);
        retryState.setFinalResponseAction(createEvaluateAction(CasWebflowConstants.ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY));
    }
}
