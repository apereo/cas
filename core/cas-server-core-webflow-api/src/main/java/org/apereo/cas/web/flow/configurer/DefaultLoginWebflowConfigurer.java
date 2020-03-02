package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.authentication.PrincipalException;
import org.apereo.cas.authentication.adaptive.UnauthorizedAuthenticationException;
import org.apereo.cas.authentication.credential.RememberMeUsernamePasswordCredential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
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
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.History;
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
 * This is {@link DefaultLoginWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultLoginWebflowConfigurer extends AbstractCasWebflowConfigurer {

    /**
     * Instantiates a new Default webflow configurer.
     *
     * @param flowBuilderServices    the flow builder services
     * @param flowDefinitionRegistry the flow definition registry
     * @param applicationContext     the application context
     * @param casProperties          the cas properties
     */
    public DefaultLoginWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                         final FlowDefinitionRegistry flowDefinitionRegistry,
                                         final ConfigurableApplicationContext applicationContext,
                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();

        if (flow != null) {
            createInitialFlowActions(flow);
            createDefaultGlobalExceptionHandlers(flow);
            createDefaultEndStates(flow);
            createDefaultDecisionStates(flow);
            createDefaultActionStates(flow);
            createDefaultViewStates(flow);
            createRememberMeAuthnWebflowConfig(flow);

            setStartState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
        }
    }

    /**
     * Create initial flow actions.
     *
     * @param flow the flow
     */
    protected void createInitialFlowActions(final Flow flow) {
        val startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
        startActionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE));
    }

    /**
     * Create default view states.
     *
     * @param flow the flow
     */
    protected void createDefaultViewStates(final Flow flow) {
        createLoginFormView(flow);
        createAuthenticationWarningMessagesView(flow);
    }

    /**
     * Create login form view.
     *
     * @param flow the flow
     */
    protected void createLoginFormView(final Flow flow) {
        val propertiesToBind = CollectionUtils.wrapList("username", "password", "source");
        val binder = createStateBinderConfiguration(propertiesToBind);

        casProperties.getView().getCustomLoginFormFields()
            .forEach((field, props) -> {
                val fieldName = String.format("customFields[%s]", field);
                binder.addBinding(new BinderConfiguration.Binding(fieldName, props.getConverter(), props.isRequired()));
            });

        val state = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "casLoginView", binder);
        state.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_RENDER_LOGIN_FORM));
        createStateModelBinding(state, CasWebflowConstants.VAR_ID_CREDENTIAL, UsernamePasswordCredential.class);

        val transition = createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);
    }

    /**
     * Create authentication warning messages view.
     *
     * @param flow the flow
     */
    protected void createAuthenticationWarningMessagesView(final Flow flow) {
        val state = createViewState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS, "casLoginMessageView");

        val setAction = createSetAction("requestScope.messages", "messageContext.allMessages");
        state.getEntryActionList().add(setAction);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PROCEED,
            CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);

        val proceedAction = createActionState(flow, CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);
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
            val state = getState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, ViewState.class);
            val cfg = getViewStateBinderConfiguration(state);
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
        createRealSubmitAction(flow);
        createInitialAuthenticationRequestValidationCheckAction(flow);
        createCreateTicketGrantingTicketAction(flow);
        createSendTicketGrantingTicketAction(flow);
        createGenerateServiceTicketAction(flow);
        createGatewayServicesMgmtAction(flow);
        createServiceAuthorizationCheckAction(flow);
        createRedirectToServiceActionState(flow);
        createHandleAuthenticationFailureAction(flow);
        createTerminateSessionAction(flow);
        createTicketGrantingTicketCheckAction(flow);
    }

    /**
     * Create real submit action.
     *
     * @param flow the flow
     */
    protected void createRealSubmitAction(final Flow flow) {
        val state = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT, CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, CasWebflowConstants.STATE_ID_SERVICE_CHECK);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
    }

    /**
     * Create ticket granting ticket check action.
     *
     * @param flow the flow
     */
    protected void createTicketGrantingTicketCheckAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK,
            CasWebflowConstants.ACTION_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS,
            CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID,
            CasWebflowConstants.STATE_ID_TERMINATE_SESSION);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID,
            CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK);
    }

    /**
     * Create initial authentication request validation check action.
     *
     * @param flow the flow
     */
    protected void createInitialAuthenticationRequestValidationCheckAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK,
            CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION);
        action.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE));
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
            CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
            CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
    }

    /**
     * Create terminate session action.
     *
     * @param flow the flow
     */
    protected void createTerminateSessionAction(final Flow flow) {
        val terminateSession = createActionState(flow,
            CasWebflowConstants.STATE_ID_TERMINATE_SESSION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION));
        createStateDefaultTransition(terminateSession, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
    }

    /**
     * Create send ticket granting ticket action.
     *
     * @param flow the flow
     */
    protected void createSendTicketGrantingTicketAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET,
            CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_SERVICE_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
            CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
    }

    /**
     * Create create ticket granting ticket action.
     *
     * @param flow the flow
     */
    protected void createCreateTicketGrantingTicketAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET, CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
    }

    /**
     * Create generate service ticket action.
     *
     * @param flow the flow
     */
    protected void createGenerateServiceTicketAction(final Flow flow) {
        val handler = createActionState(flow,
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
        val handler = createActionState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
            CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER);
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
        val redirectToView = createActionState(flow, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE);
        createTransitionForState(redirectToView, Response.ResponseType.POST.name().toLowerCase(), CasWebflowConstants.STATE_ID_POST_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.HEADER.name().toLowerCase(), CasWebflowConstants.STATE_ID_HEADER_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.REDIRECT.name().toLowerCase(), CasWebflowConstants.STATE_ID_REDIRECT_VIEW);
        redirectToView.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

    /**
     * Create service authorization check action.
     *
     * @param flow the flow
     */
    protected void createServiceAuthorizationCheckAction(final Flow flow) {
        val serviceAuthorizationCheck = createActionState(flow,
            CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK, "serviceAuthorizationCheck");
        createStateDefaultTransition(serviceAuthorizationCheck, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }

    /**
     * Create gateway services mgmt action.
     *
     * @param flow the flow
     */
    protected void createGatewayServicesMgmtAction(final Flow flow) {
        val gatewayServicesManagementCheck = createActionState(flow,
            CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT_CHECK, "gatewayServicesManagementCheck");
        createTransitionForState(gatewayServicesManagementCheck, CasWebflowConstants.STATE_ID_SUCCESS, CasWebflowConstants.STATE_ID_REDIRECT);
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
        createInjectHeadersActionState(flow);
        createGenericLoginSuccessEndState(flow);
        createServiceWarningViewState(flow);
        createEndWebflowEndState(flow);
    }

    /**
     * Create end webflow end state.
     *
     * @param flow the flow
     */
    protected void createEndWebflowEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_END_WEBFLOW);
    }

    /**
     * Create redirect end state.
     *
     * @param flow the flow
     */
    protected void createRedirectEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_REDIRECT_VIEW, "requestScope.url", true);
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
    protected void createInjectHeadersActionState(final Flow flow) {
        val headerState = createActionState(flow, CasWebflowConstants.STATE_ID_HEADER_VIEW, "injectResponseHeadersAction");
        createTransitionForState(headerState, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_END_WEBFLOW);
        createTransitionForState(headerState, CasWebflowConstants.TRANSITION_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT_VIEW);
    }

    /**
     * Create redirect unauthorized service url end state.
     *
     * @param flow the flow
     */
    protected void createRedirectUnauthorizedServiceUrlEndState(final Flow flow) {
        val state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL, "flowScope.unauthorizedRedirectUrl", true);
        state.getEntryActionList().add(createEvaluateAction("redirectUnauthorizedServiceUrlAction"));
    }

    /**
     * Create service error end state.
     *
     * @param flow the flow
     */
    protected void createServiceErrorEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR, CasWebflowConstants.VIEW_ID_SERVICE_ERROR);
    }

    /**
     * Create generic login success end state.
     *
     * @param flow the flow
     */
    protected void createGenericLoginSuccessEndState(final Flow flow) {
        val state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS, CasWebflowConstants.VIEW_ID_GENERIC_SUCCESS);
        state.getEntryActionList().add(createEvaluateAction("genericSuccessViewAction"));
    }

    /**
     * Create service warning view state.
     *
     * @param flow the flow
     */
    protected void createServiceWarningViewState(final Flow flow) {
        val stateWarning = createViewState(flow, CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW, CasWebflowConstants.VIEW_ID_CONFIRM);
        createTransitionForState(stateWarning, CasWebflowConstants.TRANSITION_ID_SUCCESS, "finalizeWarning");
        val finalizeWarn = createActionState(flow, "finalizeWarning", createEvaluateAction("serviceWarningAction"));
        createTransitionForState(finalizeWarn, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT);
    }

    /**
     * Create default global exception handlers.
     *
     * @param flow the flow
     */
    protected void createDefaultGlobalExceptionHandlers(final Flow flow) {
        val h = new TransitionExecutingFlowExecutionExceptionHandler();
        h.add(UnauthorizedSsoServiceException.class, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        h.add(NoSuchFlowExecutionException.class, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
        h.add(UnauthorizedServiceException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        h.add(UnauthorizedServiceForPrincipalException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        h.add(PrincipalException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
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
        createRenewCheckActionState(flow);
    }

    /**
     * Create service unauthorized check decision state.
     *
     * @param flow the flow
     */
    protected void createServiceUnauthorizedCheckDecisionState(final Flow flow) {
        val decision = createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
            "flowScope.unauthorizedRedirectUrl != null",
            CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL,
            CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
        decision.getEntryActionList().add(createEvaluateAction("setServiceUnauthorizedRedirectUrlAction"));
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
     * Create renew check state.
     *
     * @param flow the flow
     */
    protected void createRenewCheckActionState(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK, CasWebflowConstants.ACTION_ID_RENEW_AUTHN_REQUEST);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_PROCEED, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_RENEW, CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK);
        createStateDefaultTransition(action, CasWebflowConstants.STATE_ID_SERVICE_AUTHZ_CHECK);
    }
}

