package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

/**
 * This is {@link DefaultWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public DefaultWebflowConfigurer(final FlowBuilderServices flowBuilderServices, final FlowDefinitionRegistry flowDefinitionRegistry) {
        super(flowBuilderServices, flowDefinitionRegistry);
    }

    @Override
    protected void doInitialize() throws Exception {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createDefaultGlobalExceptionHandlers(flow);
            createDefaultEndStates(flow);
            createDefaultDecisionStates(flow);
            createDefaultActionStates(flow);

            createRememberMeAuthnWebflowConfig(flow);
        }
    }

    private void createRememberMeAuthnWebflowConfig(final Flow flow) {
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, RememberMeUsernamePasswordCredential.class);
            final ViewState state = (ViewState) flow.getState(CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
            final BinderConfiguration cfg = getViewStateBinderConfiguration(state);
            cfg.addBinding(new BinderConfiguration.Binding("rememberMe", null, false));
        } else {
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, UsernamePasswordCredential.class);
        }
    }

    /**
     * Create default action states.
     *
     * @param flow the flow
     */
    protected void createDefaultActionStates(final Flow flow) {
        createTerminateSessionAction(flow);
        createGatewayServicesMgmtAction(flow);
        createServiceAuthorizationCheckAction(flow);
        createRedirectToServiceActionState(flow);
    }

    private void createRedirectToServiceActionState(final Flow flow) {
        final ActionState redirectToView = createActionState(flow,
                CasWebflowConstants.STATE_ID_REDIRECT,
                createEvaluateAction("redirectToServiceAction"));
        createTransitionForState(redirectToView, Response.ResponseType.POST.name().toLowerCase(), CasWebflowConstants.STATE_ID_POST_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.REDIRECT.name().toLowerCase(), CasWebflowConstants.STATE_ID_REDIR_VIEW);
    }

    private void createServiceAuthorizationCheckAction(final Flow flow) {
        final ActionState serviceAuthorizationCheck = createActionState(flow,
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK, createEvaluateAction("serviceAuthorizationCheck"));
        createStateDefaultTransition(serviceAuthorizationCheck, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }

    private void createGatewayServicesMgmtAction(final Flow flow) {
        final ActionState gatewayServicesManagementCheck = createActionState(flow,
                CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK, createEvaluateAction("gatewayServicesManagementCheck"));
        createTransitionForState(gatewayServicesManagementCheck, CasWebflowConstants.STATE_ID_SUCCESS,
                CasWebflowConstants.STATE_ID_REDIRECT);
    }

    private void createTerminateSessionAction(final Flow flow) {
        final ActionState terminateSession = createActionState(flow,
                CasWebflowConstants.STATE_ID_TERMINATE_SESSION, createEvaluateAction("terminateSessionAction"));
        createStateDefaultTransition(terminateSession, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
    }

    /**
     * Create default global exception handlers.
     *
     * @param flow the flow
     */
    protected void createDefaultEndStates(final Flow flow) {
        createRedirectUnauthorizedServiceUrlEndState(flow);
        createServiceErrorEndState(flow);
        createRedirectEndState(flow);
        createPostEndState(flow);
        createGenericLoginSuccessEndState(flow);
        createServiceWarningViewState(flow);
    }

    private void createRedirectEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_REDIR_VIEW, "requestScope.url", true);
    }

    private void createPostEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_POST_VIEW, CasWebflowConstants.VIEW_ID_POST_RESPONSE);
    }

    private void createRedirectUnauthorizedServiceUrlEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL, "flowScope.unauthorizedRedirectUrl", true);
    }

    private void createServiceErrorEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR, CasWebflowConstants.VIEW_ID_SERVICE_ERROR);
    }

    private void createGenericLoginSuccessEndState(final Flow flow) {
        final EndState state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS, CasWebflowConstants.VIEW_ID_GENERIC_SUCCESS);
        state.getEntryActionList().add(createEvaluateAction("genericSuccessViewAction"));
    }

    private void createServiceWarningViewState(final Flow flow) {
        final ViewState stateWarning = createViewState(flow, CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW, CasWebflowConstants.VIEW_ID_CONFIRM);
        createTransitionForState(stateWarning, CasWebflowConstants.TRANSITION_ID_SUCCESS, "finalizeWarning");
        final ActionState finalizeWarn = createActionState(flow, "finalizeWarning", createEvaluateAction("serviceWarningAction"));
        createTransitionForState(finalizeWarn, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT);
    }

    /**
     * Create default global exception handlers.
     *
     * @param flow the flow
     */
    protected void createDefaultGlobalExceptionHandlers(final Flow flow) {
        final TransitionExecutingFlowExecutionExceptionHandler h = new TransitionExecutingFlowExecutionExceptionHandler();
        h.add(UnauthorizedSsoServiceException.class, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        h.add(NoSuchFlowExecutionException.class, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
        h.add(UnauthorizedServiceException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        h.add(UnauthorizedServiceForPrincipalException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        flow.getExceptionHandlerSet().add(h);
    }

    /**
     * Create default decision states.
     *
     * @param flow the flow
     */
    protected void createDefaultDecisionStates(final Flow flow) {
        createServiceUnauthorizedCheckDecisionState(flow);
        createServiceCheckDecisionState(flow);
        createWarnDecisionState(flow);
        createGatewayRequestCheckDecisionState(flow);
        createHasServiceCheckDecisionState(flow);
        createRenewCheckDecisionState(flow);
    }

    private void createServiceUnauthorizedCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
                "flowScope.unauthorizedRedirectUrl != null",
                CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL,
                CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
    }

    private void createServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_CHECK,
                "flowScope.service != null",
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
                CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    private void createWarnDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_WARN,
                "flowScope.warnCookieValue",
                CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW,
                CasWebflowConstants.STATE_ID_REDIRECT);
    }

    private void createGatewayRequestCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK,
                "requestParameters.gateway != '' and requestParameters.gateway != null and flowScope.service != null",
                CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK,
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK);
    }

    private void createHasServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK,
                "flowScope.service != null",
                CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
                CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    private void createRenewCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
                "requestParameters.renew != '' and requestParameters.renew != null",
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK,
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
    }
}

