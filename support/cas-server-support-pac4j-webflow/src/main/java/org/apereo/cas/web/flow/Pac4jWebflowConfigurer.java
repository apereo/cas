package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.web.flow.DelegatedClientAuthenticationAction;
import org.apereo.cas.support.pac4j.web.flow.SingleLogoutPreparationAction;
import org.apereo.cas.web.flow.configurer.AbstractCasWebflowConfigurer;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.DecisionState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.Transition;
import org.springframework.webflow.engine.TransitionableState;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * The {@link Pac4jWebflowConfigurer} is responsible for
 * adjusting the CAS webflow context for pac4j integration.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class Pac4jWebflowConfigurer extends AbstractCasWebflowConfigurer {

    private final Logger logger = LoggerFactory.getLogger(Pac4jWebflowConfigurer.class);
    
    private final Action saml2ClientLogoutAction;
    private final Action ignoreServiceRedirectForSamlSloAction;
    private final TerminateSessionAction terminateSessionAction;
    private final Action limitedTerminateSessionAction;
    
    public Pac4jWebflowConfigurer(final FlowBuilderServices flowBuilderServices, 
                                  final FlowDefinitionRegistry loginFlowDefinitionRegistry,
                                  final FlowDefinitionRegistry logoutFlowDefinitionRegistry,
                                  final Action saml2ClientLogoutAction,
                                  final Action ignoreServiceRedirectForSamlSloAction,
                                  final TerminateSessionAction terminateSessionAction,
                                  final Action limitedTerminateSessionAction,
                                  final ApplicationContext applicationContext,
                                  final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, loginFlowDefinitionRegistry, applicationContext, casProperties);
        setLogoutFlowDefinitionRegistry(logoutFlowDefinitionRegistry);
        this.saml2ClientLogoutAction = saml2ClientLogoutAction;
        this.ignoreServiceRedirectForSamlSloAction = ignoreServiceRedirectForSamlSloAction;
        this.terminateSessionAction = terminateSessionAction;
        this.limitedTerminateSessionAction = limitedTerminateSessionAction;
    }

    @Override
    protected void doInitialize() {
        final Flow loginFlow = getLoginFlow();
        if (loginFlow != null) {
            createClientActionActionState(loginFlow);
            createStopWebflowViewState(loginFlow);
            logger.debug("The Login webflow has been reconfigured by PAC4J.");
        }

        final Flow logoutFlow = getLogoutFlow();
        if (logoutFlow != null) {
            createPrepareForSingleLogoutAction(logoutFlow);
            createSaml2ClientLogoutAction(logoutFlow);
            createIgnoreServiceRedirectUrlForForSamlSloAction(logoutFlow);
            switchOffSessionInvalidationDuringFlow();
            createSessionInvalidationActionAtFlowEnd(logoutFlow);
            logger.debug("The Logout webflow has been reconfigured by PAC4J.");
        }
    }

    private void createClientActionActionState(final Flow flow) {
        final ActionState actionState = createActionState(flow, DelegatedClientAuthenticationAction.CLIENT_ACTION,
                createEvaluateAction(DelegatedClientAuthenticationAction.CLIENT_ACTION));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET));
        actionState.getTransitionSet().add(createTransition(CasWebflowConstants.TRANSITION_ID_ERROR, getStartState(flow).getId()));
        actionState.getTransitionSet().add(createTransition(DelegatedClientAuthenticationAction.STOP,
                DelegatedClientAuthenticationAction.STOP_WEBFLOW));
        setStartState(flow, actionState);
    }

    private void createStopWebflowViewState(final Flow flow) {
        final ViewState state = createViewState(flow, DelegatedClientAuthenticationAction.STOP_WEBFLOW,
                DelegatedClientAuthenticationAction.VIEW_ID_STOP_WEBFLOW);
        state.getEntryActionList().add(new AbstractAction() {
            @Override
            protected Event doExecute(final RequestContext requestContext) {
                final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
                final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                final Optional<ModelAndView> mv = DelegatedClientAuthenticationAction.hasDelegationRequestFailed(request,
                        response.getStatus());
                mv.ifPresent(modelAndView -> modelAndView.getModel().forEach((k, v) -> requestContext.getFlowScope().put(k, v)));
                return null;
            }
        });
    }


    /**
     * Adds the PAC4J Single Logout Preparation Action to the logout web flow, right at the beginning. This changes the start state. The old
     * start state will become 2nd in the flow.
     * 
     * @param logoutFlow
     *            The Logout flow.
     */
    private void createPrepareForSingleLogoutAction(final Flow logoutFlow) {
        final TransitionableState initiallyStartState = getStartState(logoutFlow);
        final Transition transitionToInitiallyStartState = createTransition(initiallyStartState.getId());

        // Create a new action state and let it transition to the previous start state
        final ActionState actionStateForPrepareSingleLogout = createActionState(logoutFlow,
               SingleLogoutPreparationAction.WEBFLOW_ACTION_STATE_ID,
               createEvaluateAction(SingleLogoutPreparationAction.WEBFLOW_ACTION_EVAL_EXPRESSION)); 
        actionStateForPrepareSingleLogout.getTransitionSet().add(transitionToInitiallyStartState);

        // Set a new start state
        setStartState(logoutFlow, actionStateForPrepareSingleLogout);
    }


    /**
     * Adds the PAC4J Client Logout Action to the logout web flow, to state "finishLogout", on-entry section.
     * 
     * @param logoutFlow
     *            The Logout flow.
     */
    private void createSaml2ClientLogoutAction(final Flow logoutFlow) {
        final DecisionState state = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        state.getEntryActionList().add(saml2ClientLogoutAction);
    }

    /**
     * Adds the PAC4J Ignore Service Redirect URL Action to the logout web flow, to state "finishLogout", on-entry section.
     * 
     * @param logoutFlow
     *            The Logout flow.
     */
    private void createIgnoreServiceRedirectUrlForForSamlSloAction(final Flow logoutFlow) {
        final DecisionState state = getState(logoutFlow, CasWebflowConstants.STATE_ID_FINISH_LOGOUT, DecisionState.class);
        state.getEntryActionList().add(ignoreServiceRedirectForSamlSloAction);
    }

    /**
     * Locates the Terminate Session Action and switches actual HTTP session invalidation off.
     */
    private void switchOffSessionInvalidationDuringFlow() {
        terminateSessionAction.setApplicationSessionDestroyDeferred(true);
    }


    /**
     * Adds the Limited Terminate Session Action to the end of the Logout flow, without any reference to a state. The action will be
     * executed when the flow ends.
     * 
     * @param logoutFlow
     *            The Logout flow.
     */
    private void createSessionInvalidationActionAtFlowEnd(final Flow logoutFlow) {
        logoutFlow.getEndActionList().add(limitedTerminateSessionAction);
    }

}
