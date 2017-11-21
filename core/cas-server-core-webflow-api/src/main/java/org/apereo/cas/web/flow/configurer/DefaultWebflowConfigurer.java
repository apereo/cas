package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.exceptions.AccountDisabledException;
import org.apereo.cas.authentication.exceptions.AccountPasswordMustChangeException;
import org.apereo.cas.authentication.exceptions.InvalidLoginLocationException;
import org.apereo.cas.authentication.exceptions.InvalidLoginTimeException;
import org.apereo.cas.authentication.principal.Response;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.UnauthorizedServiceForPrincipalException;
import org.apereo.cas.services.UnauthorizedSsoServiceException;
import org.apereo.cas.ticket.UnsatisfiedAuthenticationPolicyException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.SetAction;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.ActionState;
import org.springframework.webflow.engine.EndState;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;

import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

/**
 * This is {@link DefaultWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public DefaultWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                    final FlowDefinitionRegistry flowDefinitionRegistry,
                                    final ApplicationContext applicationContext,
                                    final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        final Flow flow = getLoginFlow();

        if (flow != null) {
            createInitialFlowActions(flow);
            createDefaultGlobalExceptionHandlers(flow);
            createDefaultEndStates(flow);
            createDefaultDecisionStates(flow);
            createDefaultActionStates(flow);
            createDefaultViewStates(flow);
            createRememberMeAuthnWebflowConfig(flow);
        }
    }

    /**
     * Create initial flow actions.
     *
     * @param flow the flow
     */
    protected void createInitialFlowActions(final Flow flow) {
        flow.getStartActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INIT_FLOW_SETUP));
    }

    /**
     * Create default view states.
     *
     * @param flow the flow
     */
    protected void createDefaultViewStates(final Flow flow) {
        createAuthenticationWarningMessagesView(flow);
    }

    /**
     * Create authentication warning messages view.
     *
     * @param flow the flow
     */
    protected void createAuthenticationWarningMessagesView(final Flow flow) {
        final ViewState state = createViewState(flow, CasWebflowConstants.VIEW_ID_SHOW_AUTHN_WARNING_MSGS, "casLoginMessageView");

        final SetAction setAction = new SetAction(createExpression("requestScope.messages"), createExpression("messageContext.allMessages"));
        state.getEntryActionList().add(setAction);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PROCEED, CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);

        final ActionState proceedAction = createActionState(flow, CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);
        proceedAction.getActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET));
        createStateDefaultTransition(proceedAction, CasWebflowConstants.STATE_ID_SERVICE_CHECK);
    }

    /**
     * Create remember me authn webflow config.
     *
     * @param flow the flow
     */
    protected void createRememberMeAuthnWebflowConfig(final Flow flow) {
        if (casProperties.getTicket().getTgt().getRememberMe().isEnabled()) {
            createFlowVariable(flow, CasWebflowConstants.VAR_ID_CREDENTIAL, RememberMeUsernamePasswordCredential.class);
            final ViewState state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
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
        createSendTicketGrantingTicketAction(flow);
        createGenerateServiceTicketAction(flow);
        createTerminateSessionAction(flow);
        createGatewayServicesMgmtAction(flow);
        createServiceAuthorizationCheckAction(flow);
        createRedirectToServiceActionState(flow);
        createHandleAuthenticationFailureAction(flow);
    }

    private void createSendTicketGrantingTicketAction(final Flow flow) {
        final ActionState action = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET));
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SERVICE_CHECK);
    }

    /**
     * Create generate service ticket action.
     *
     * @param flow the flow
     */
    protected void createGenerateServiceTicketAction(final Flow flow) {
        final ActionState handler = createActionState(flow,
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET));
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_REDIRECT);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_GATEWAY, CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK);
    }

    /**
     * Create handle authentication failure action.
     *
     * @param flow the flow
     */
    protected void createHandleAuthenticationFailureAction(final Flow flow) {
        final ActionState handler = createActionState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER));
        createTransitionForState(handler, AccountDisabledException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_DISABLED);
        createTransitionForState(handler, AccountLockedException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_ACCOUNT_LOCKED);
        createTransitionForState(handler, AccountPasswordMustChangeException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_MUST_CHANGE_PASSWORD);
        createTransitionForState(handler, CredentialExpiredException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_EXPIRED_PASSWORD);
        createTransitionForState(handler, InvalidLoginLocationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_WORKSTATION);
        createTransitionForState(handler, InvalidLoginTimeException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_INVALID_AUTHENTICATION_HOURS);
        createTransitionForState(handler, FailedLoginException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, AccountNotFoundException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedServiceForPrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, PrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, UnauthorizedAuthenticationException.class.getSimpleName(), CasWebflowConstants.VIEW_ID_AUTHENTICATION_BLOCKED);
        createTransitionForState(handler, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        createStateDefaultTransition(handler, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);

    }

    /**
     * Create redirect to service action state.
     *
     * @param flow the flow
     */
    protected void createRedirectToServiceActionState(final Flow flow) {
        final ActionState redirectToView = createActionState(flow, CasWebflowConstants.STATE_ID_REDIRECT,
                createEvaluateAction(CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE));
        createTransitionForState(redirectToView, Response.ResponseType.POST.name().toLowerCase(), CasWebflowConstants.STATE_ID_POST_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.HEADER.name().toLowerCase(), CasWebflowConstants.STATE_ID_HEADER_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.REDIRECT.name().toLowerCase(), CasWebflowConstants.STATE_ID_REDIR_VIEW);
    }

    /**
     * Create service authorization check action.
     *
     * @param flow the flow
     */
    private void createServiceAuthorizationCheckAction(final Flow flow) {
        final ActionState serviceAuthorizationCheck = createActionState(flow,
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK, createEvaluateAction("serviceAuthorizationCheck"));
        createStateDefaultTransition(serviceAuthorizationCheck, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }

    /**
     * Create gateway services mgmt action.
     *
     * @param flow the flow
     */
    protected void createGatewayServicesMgmtAction(final Flow flow) {
        final ActionState gatewayServicesManagementCheck = createActionState(flow,
                CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK, createEvaluateAction("gatewayServicesManagementCheck"));
        createTransitionForState(gatewayServicesManagementCheck, CasWebflowConstants.STATE_ID_SUCCESS, CasWebflowConstants.STATE_ID_REDIRECT);
    }

    /**
     * Create terminate session action.
     *
     * @param flow the flow
     */
    protected void createTerminateSessionAction(final Flow flow) {
        final ActionState terminateSession = createActionState(flow,
                CasWebflowConstants.STATE_ID_TERMINATE_SESSION, createEvaluateAction(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION));
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
        createHeaderEndState(flow);
        createGenericLoginSuccessEndState(flow);
        createServiceWarningViewState(flow);
    }

    /**
     * Create redirect end state.
     *
     * @param flow the flow
     */
    protected void createRedirectEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_REDIR_VIEW, "requestScope.url", true);
    }

    /**
     * Create post end state.
     *
     * @param flow the flow
     */
    protected void createPostEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_POST_VIEW, CasWebflowConstants.VIEW_ID_POST_RESPONSE);
    }

    /**
     * Create header end state.
     *
     * @param flow the flow
     */
    protected void createHeaderEndState(final Flow flow) {
        final EndState endState = createEndState(flow, CasWebflowConstants.STATE_ID_HEADER_VIEW);
        endState.setFinalResponseAction(createEvaluateAction("injectResponseHeadersAction"));
    }

    /**
     * Create redirect unauthorized service url end state.
     *
     * @param flow the flow
     */
    protected void createRedirectUnauthorizedServiceUrlEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL, "flowScope.unauthorizedRedirectUrl", true);
    }

    /**
     * Create service error end state.
     *
     * @param flow the flow
     */
    private void createServiceErrorEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR, CasWebflowConstants.VIEW_ID_SERVICE_ERROR);
    }

    /**
     * Create generic login success end state.
     *
     * @param flow the flow
     */
    private void createGenericLoginSuccessEndState(final Flow flow) {
        final EndState state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS, CasWebflowConstants.VIEW_ID_GENERIC_SUCCESS);
        state.getEntryActionList().add(createEvaluateAction("genericSuccessViewAction"));
    }

    /**
     * Create service warning view state.
     *
     * @param flow the flow
     */
    protected void createServiceWarningViewState(final Flow flow) {
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

    /**
     * Create service unauthorized check decision state.
     *
     * @param flow the flow
     */
    protected void createServiceUnauthorizedCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
                "flowScope.unauthorizedRedirectUrl != null",
                CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL,
                CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
    }

    /**
     * Create service check decision state.
     *
     * @param flow the flow
     */
    protected void createServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_CHECK,
                "flowScope.service != null",
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
                CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    /**
     * Create warn decision state.
     *
     * @param flow the flow
     */
    protected void createWarnDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_WARN,
                "flowScope.warnCookieValue",
                CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW,
                CasWebflowConstants.STATE_ID_REDIRECT);
    }

    /**
     * Create gateway request check decision state.
     *
     * @param flow the flow
     */
    protected void createGatewayRequestCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK,
                "requestParameters.gateway != '' and requestParameters.gateway != null and flowScope.service != null",
                CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK,
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK);
    }

    /**
     * Create has service check decision state.
     *
     * @param flow the flow
     */
    protected void createHasServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK,
                "flowScope.service != null",
                CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
                CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    /**
     * Create renew check decision state.
     *
     * @param flow the flow
     */
    protected void createRenewCheckDecisionState(final Flow flow) {
        final String renewParam = "requestParameters." + CasProtocolConstants.PARAMETER_RENEW;
        createDecisionState(flow, CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
                renewParam + " != '' and " + renewParam + " != null",
                CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK,
                CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
    }
}

