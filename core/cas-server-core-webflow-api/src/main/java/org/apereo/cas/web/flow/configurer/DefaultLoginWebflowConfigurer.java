package org.apereo.cas.web.flow.configurer;

import org.apereo.cas.CasProtocolConstants;
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
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.StringToCharArrayConverter;
import org.apereo.cas.web.flow.actions.ConsumerExecutionAction;
import org.apereo.cas.web.flow.resolver.DynamicTargetStateResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.definition.registry.FlowDefinitionRegistry;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.History;
import org.springframework.webflow.engine.NoMatchingTransitionException;
import org.springframework.webflow.engine.ViewState;
import org.springframework.webflow.engine.builder.BinderConfiguration;
import org.springframework.webflow.engine.builder.support.FlowBuilderServices;
import org.springframework.webflow.engine.support.TransitionExecutingFlowExecutionExceptionHandler;
import org.springframework.webflow.execution.repository.NoSuchFlowExecutionException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import java.util.Locale;
import java.util.Map;

/**
 * This is {@link DefaultLoginWebflowConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DefaultLoginWebflowConfigurer extends AbstractCasWebflowConfigurer {

    public DefaultLoginWebflowConfigurer(final FlowBuilderServices flowBuilderServices,
                                         final FlowDefinitionRegistry flowDefinitionRegistry,
                                         final ConfigurableApplicationContext applicationContext,
                                         final CasConfigurationProperties casProperties) {
        super(flowBuilderServices, flowDefinitionRegistry, applicationContext, casProperties);
    }

    @Override
    protected void doInitialize() {
        val flow = getLoginFlow();
        createInitialFlowActions(flow);
        createDefaultGlobalExceptionHandlers(flow);
        createDefaultEndStates(flow);
        createDefaultDecisionStates(flow);
        createDefaultActionStates(flow);
        createDefaultViewStates(flow);
        createRememberMeAuthnWebflowConfig(flow);
        setStartState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK);
    }

    protected void createInitialFlowActions(final Flow flow) {
        val startActionList = flow.getStartActionList();
        startActionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_INITIAL_FLOW_SETUP));
        startActionList.add(createEvaluateAction(CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE));
    }

    protected void createDefaultViewStates(final Flow flow) {
        createLoginFormView(flow);
        createAuthenticationWarningMessagesView(flow);
        createSessionStorageStates(flow);
    }

    protected void createLoginFormView(final Flow flow) {
        val propertiesToBind = Map.of(
            CasProtocolConstants.PARAMETER_USERNAME, Map.of("required", "true"),
            CasProtocolConstants.PARAMETER_PASSWORD, Map.of("converter", StringToCharArrayConverter.ID),
            "source", Map.of("required", "true"));
        val binder = createStateBinderConfiguration(propertiesToBind);
        casProperties.getView().getCustomLoginFormFields()
            .forEach((field, props) -> {
                val fieldName = String.format("customFields[%s]", field);
                binder.addBinding(new BinderConfiguration.Binding(fieldName, props.getConverter(), props.isRequired()));
            });

        val state = createViewState(flow, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM, "login/casLoginView", binder);
        createStateModelBinding(state, CasWebflowConstants.VAR_ID_CREDENTIAL, UsernamePasswordCredential.class);

        val transition = createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUBMIT, CasWebflowConstants.STATE_ID_REAL_SUBMIT);
        val attributes = transition.getAttributes();
        attributes.put("bind", Boolean.TRUE);
        attributes.put("validate", Boolean.TRUE);
        attributes.put("history", History.INVALIDATE);
    }

    protected void createAuthenticationWarningMessagesView(final Flow flow) {
        val state = createViewState(flow, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS, "login/casLoginMessageView");

        val setAction = createSetAction("requestScope.messages", "messageContext.allMessages");
        state.getEntryActionList().add(setAction);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_PROCEED, CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);
        val proceedAction = createActionState(flow, CasWebflowConstants.STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW);
        proceedAction.getActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET));
        createStateDefaultTransition(proceedAction, CasWebflowConstants.STATE_ID_SERVICE_CHECK);
    }

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

    protected void createDefaultActionStates(final Flow flow) {
        createInitialLoginAction(flow);
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

    protected void createRealSubmitAction(final Flow flow) {
        val state = createActionState(flow, CasWebflowConstants.STATE_ID_REAL_SUBMIT,
            CasWebflowConstants.ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION);

        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_WARN, CasWebflowConstants.STATE_ID_WARN);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_TICKET_GRANTING_TICKET_VALID, CasWebflowConstants.STATE_ID_SERVICE_CHECK);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_GENERATE_SERVICE_TICKET, CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
    }

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

    protected void createInitialAuthenticationRequestValidationCheckAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK,
            CasWebflowConstants.ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION);
        action.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_VERIFY_REQUIRED_SERVICE));
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_ERROR, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS, CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ);
    }

    protected void createTerminateSessionAction(final Flow flow) {
        val terminateSession = createActionState(flow,
            CasWebflowConstants.STATE_ID_TERMINATE_SESSION,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION));
        createStateDefaultTransition(terminateSession, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK);
    }

    protected void createSendTicketGrantingTicketAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET,
            CasWebflowConstants.ACTION_ID_SEND_TICKET_GRANTING_TICKET);
        action.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SINGLE_SIGON_SESSION_CREATED));

        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_WRITE_BROWSER_STORAGE,
            CasWebflowConstants.STATE_ID_BROWSER_STORAGE_WRITE);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_SERVICE_CHECK);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
            CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);

    }

    private void createSessionStorageStates(final Flow flow) {
        val writeStorage = createViewState(flow, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_WRITE, CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_WRITE);
        writeStorage.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_WRITE_BROWSER_STORAGE));
        createTransitionForState(writeStorage, CasWebflowConstants.TRANSITION_ID_CONTINUE, CasWebflowConstants.STATE_ID_SERVICE_CHECK);

        val readStorage = createViewState(flow, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ, CasWebflowConstants.VIEW_ID_BROWSER_STORAGE_READ);
        readStorage.getRenderActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_PUT_BROWSER_STORAGE));
        createTransitionForState(readStorage, CasWebflowConstants.TRANSITION_ID_CONTINUE, CasWebflowConstants.STATE_ID_VERIFY_BROWSER_STORAGE_READ);

        val verifyStorage = createActionState(flow, CasWebflowConstants.STATE_ID_VERIFY_BROWSER_STORAGE_READ, CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE);
        createTransitionForState(verifyStorage, CasWebflowConstants.TRANSITION_ID_SUCCESS, new DynamicTargetStateResolver(flow));
        createTransitionForState(verifyStorage, CasWebflowConstants.TRANSITION_ID_SKIP, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(verifyStorage, CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE, CasWebflowConstants.STATE_ID_BROWSER_STORAGE_READ);
    }

    protected void createCreateTicketGrantingTicketAction(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_CREATE_TICKET_GRANTING_TICKET,
            CasWebflowConstants.ACTION_ID_CREATE_TICKET_GRANTING_TICKET);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS_WITH_WARNINGS,
            CasWebflowConstants.STATE_ID_SHOW_AUTHN_WARNING_MSGS);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_SEND_TICKET_GRANTING_TICKET);
    }

    protected void createGenerateServiceTicketAction(final Flow flow) {
        val handler = createActionState(flow,
            CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
            createEvaluateAction(CasWebflowConstants.ACTION_ID_GENERATE_SERVICE_TICKET));
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_REDIRECT);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_WARN,
            CasWebflowConstants.STATE_ID_WARN);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE,
            CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_ERROR,
            CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(handler, CasWebflowConstants.TRANSITION_ID_GATEWAY,
            CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT);
    }

    protected void createHandleAuthenticationFailureAction(final Flow flow) {
        val authnFailure = createActionState(flow, CasWebflowConstants.STATE_ID_HANDLE_AUTHN_FAILURE,
            CasWebflowConstants.ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER);

        createTransitionForState(authnFailure, AccountDisabledException.class.getSimpleName(), CasWebflowConstants.STATE_ID_ACCOUNT_DISABLED);
        createTransitionForState(authnFailure, AccountLockedException.class.getSimpleName(), CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED);
        createTransitionForState(authnFailure, AccountExpiredException.class.getSimpleName(), CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
        createTransitionForState(authnFailure, AccountLockedException.class.getSimpleName(), CasWebflowConstants.STATE_ID_ACCOUNT_LOCKED);
        createTransitionForState(authnFailure, AccountPasswordMustChangeException.class.getSimpleName(), CasWebflowConstants.STATE_ID_MUST_CHANGE_PASSWORD);
        createTransitionForState(authnFailure, CredentialExpiredException.class.getSimpleName(), CasWebflowConstants.STATE_ID_EXPIRED_PASSWORD);
        createTransitionForState(authnFailure, InvalidLoginLocationException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INVALID_WORKSTATION);
        createTransitionForState(authnFailure, InvalidLoginTimeException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INVALID_AUTHENTICATION_HOURS);
        createTransitionForState(authnFailure, FailedLoginException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(authnFailure, AccountNotFoundException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(authnFailure, UnauthorizedServiceForPrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(authnFailure, PrincipalException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(authnFailure, UnsatisfiedAuthenticationPolicyException.class.getSimpleName(), CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        createTransitionForState(authnFailure, UnauthorizedAuthenticationException.class.getSimpleName(), CasWebflowConstants.STATE_ID_AUTHENTICATION_BLOCKED);
        createTransitionForState(authnFailure, CasWebflowConstants.TRANSITION_ID_SERVICE_UNAUTHZ_CHECK, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        createTransitionForState(authnFailure, CasWebflowConstants.TRANSITION_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT_VIEW);

        createStateDefaultTransition(authnFailure, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
        authnFailure.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

    protected void createRedirectToServiceActionState(final Flow flow) {
        val redirectToView = createActionState(flow, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.ACTION_ID_REDIRECT_TO_SERVICE);
        createTransitionForState(redirectToView, Response.ResponseType.POST.name().toLowerCase(Locale.ENGLISH), CasWebflowConstants.STATE_ID_POST_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.HEADER.name().toLowerCase(Locale.ENGLISH), CasWebflowConstants.STATE_ID_HEADER_VIEW);
        createTransitionForState(redirectToView, Response.ResponseType.REDIRECT.name().toLowerCase(Locale.ENGLISH), CasWebflowConstants.STATE_ID_REDIRECT_VIEW);
        redirectToView.getExitActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS));
    }

    protected void createServiceAuthorizationCheckAction(final Flow flow) {
        val serviceAuthorizationCheck = createActionState(flow,
            CasWebflowConstants.STATE_ID_SERVICE_AUTHZ, CasWebflowConstants.ACTION_ID_SERVICE_AUTHZ_CHECK);
        createStateDefaultTransition(serviceAuthorizationCheck, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM);
    }

    protected void createGatewayServicesMgmtAction(final Flow flow) {
        val gatewayServicesManagementCheck = createActionState(flow,
            CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT,
            CasWebflowConstants.ACTION_ID_GATEWAY_SERVICES_MANAGEMENT);
        createTransitionForState(gatewayServicesManagementCheck, CasWebflowConstants.STATE_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_REDIRECT);
    }

    protected void createDefaultEndStates(final Flow flow) {
        createRedirectUnauthorizedServiceUrlEndState(flow);
        createServiceErrorEndState(flow);
        createWebflowConfigurationErrorEndState(flow);
        createServiceErrorEndState(flow);
        createRedirectEndState(flow);
        createPostEndState(flow);
        createInjectHeadersActionState(flow);
        createGenericLoginSuccessEndState(flow);
        createServiceWarningViewState(flow);
        createEndWebflowEndState(flow);
    }

    protected void createEndWebflowEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_END_WEBFLOW);
    }

    protected void createRedirectEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_REDIRECT_VIEW, "requestScope.url", true);
    }

    protected void createPostEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_POST_VIEW, CasWebflowConstants.VIEW_ID_POST_RESPONSE);
    }

    protected void createInjectHeadersActionState(final Flow flow) {
        val headerState = createActionState(flow, CasWebflowConstants.STATE_ID_HEADER_VIEW,
            CasWebflowConstants.ACTION_ID_INJECT_RESPONSE_HEADERS);
        createTransitionForState(headerState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_END_WEBFLOW);
        createTransitionForState(headerState, CasWebflowConstants.TRANSITION_ID_REDIRECT,
            CasWebflowConstants.STATE_ID_REDIRECT_VIEW);
    }

    protected void createRedirectUnauthorizedServiceUrlEndState(final Flow flow) {
        val state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL, "error/casUnauthorizedServiceRedirectView");
        state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_REDIRECT_UNAUTHORIZED_SERVICE_URL));
    }

    protected void createServiceErrorEndState(final Flow flow) {
        createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR, CasWebflowConstants.VIEW_ID_SERVICE_ERROR);
    }

    protected void createWebflowConfigurationErrorEndState(final Flow flow) {
        val state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR, CasWebflowConstants.VIEW_ID_WEBFLOW_CONFIG_ERROR);
        state.getEntryActionList().add(new ConsumerExecutionAction(context -> {
            if (context.getFlashScope().contains(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION)) {
                val rootException = (Exception) context.getFlashScope().get(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION);
                LoggingUtils.error(LOGGER, rootException);
            }
        }));
    }

    protected void createGenericLoginSuccessEndState(final Flow flow) {
        val state = createEndState(flow, CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS, "login/casGenericSuccessView");
        state.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_GENERIC_SUCCESS_VIEW));
    }

    protected void createServiceWarningViewState(final Flow flow) {
        val stateWarning = createViewState(flow, CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW, "login/casConfirmView");
        createTransitionForState(stateWarning, CasWebflowConstants.TRANSITION_ID_SUCCESS, CasWebflowConstants.STATE_ID_FINALIZE_WARNING);
        val finalizeWarn = createActionState(flow, CasWebflowConstants.STATE_ID_FINALIZE_WARNING, CasWebflowConstants.ACTION_ID_SERVICE_WARNING);
        createTransitionForState(finalizeWarn, CasWebflowConstants.STATE_ID_REDIRECT, CasWebflowConstants.STATE_ID_REDIRECT);
    }

    protected void createDefaultGlobalExceptionHandlers(final Flow flow) {
        val handler = new TransitionExecutingFlowExecutionExceptionHandler();
        handler.add(UnauthorizedSsoServiceException.class, CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
        handler.add(NoSuchFlowExecutionException.class, CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
        handler.add(UnauthorizedServiceException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        handler.add(UnauthorizedServiceForPrincipalException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        handler.add(PrincipalException.class, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK);
        handler.add(NoMatchingTransitionException.class, CasWebflowConstants.STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR);
        flow.getExceptionHandlerSet().add(handler);
    }

    protected void createDefaultDecisionStates(final Flow flow) {
        createServiceUnauthorizedCheckDecisionState(flow);
        createServiceCheckDecisionState(flow);
        createWarnDecisionState(flow);
        createGatewayRequestCheckDecisionState(flow);
        createHasServiceCheckDecisionState(flow);
        createRenewCheckActionState(flow);
    }

    protected void createServiceUnauthorizedCheckDecisionState(final Flow flow) {
        val decision = createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_UNAUTHZ_CHECK,
            "flowScope.unauthorizedRedirectUrl != null",
            CasWebflowConstants.STATE_ID_VIEW_REDIR_UNAUTHZ_URL,
            CasWebflowConstants.STATE_ID_VIEW_SERVICE_ERROR);
        decision.getEntryActionList().add(createEvaluateAction(CasWebflowConstants.ACTION_ID_SET_SERVICE_UNAUTHORIZED_REDIRECT_URL));
    }

    protected void createServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_SERVICE_CHECK,
            "flowScope.service != null",
            CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET,
            CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    protected void createWarnDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_WARN,
            "flowScope.warnCookieValue",
            CasWebflowConstants.STATE_ID_SHOW_WARNING_VIEW,
            CasWebflowConstants.STATE_ID_REDIRECT);
    }

    protected void createGatewayRequestCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_GATEWAY_REQUEST_CHECK,
            "requestParameters.gateway != '' and requestParameters.gateway != null and flowScope.service != null",
            CasWebflowConstants.STATE_ID_GATEWAY_SERVICES_MGMT,
            CasWebflowConstants.STATE_ID_SERVICE_AUTHZ);
    }

    protected void createHasServiceCheckDecisionState(final Flow flow) {
        createDecisionState(flow, CasWebflowConstants.STATE_ID_HAS_SERVICE_CHECK,
            "flowScope.service != null",
            CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
            CasWebflowConstants.STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS);
    }

    protected void createRenewCheckActionState(final Flow flow) {
        val action = createActionState(flow, CasWebflowConstants.STATE_ID_RENEW_REQUEST_CHECK,
            CasWebflowConstants.ACTION_ID_RENEW_AUTHN_REQUEST);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_PROCEED,
            CasWebflowConstants.STATE_ID_GENERATE_SERVICE_TICKET);
        createTransitionForState(action, CasWebflowConstants.TRANSITION_ID_RENEW,
            CasWebflowConstants.STATE_ID_SERVICE_AUTHZ);
        createStateDefaultTransition(action, CasWebflowConstants.STATE_ID_SERVICE_AUTHZ);
    }

    private void createInitialLoginAction(final Flow flow) {
        val state = createActionState(flow, CasWebflowConstants.STATE_ID_INIT_LOGIN_FORM,
            CasWebflowConstants.ACTION_ID_INIT_LOGIN_ACTION);
        createTransitionForState(state, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_AFTER_INIT_LOGIN_FORM);
        val afterState = createActionState(flow,
            CasWebflowConstants.STATE_ID_AFTER_INIT_LOGIN_FORM, createSetAction("requestScope.initialized", "true"));
        createTransitionForState(afterState, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            CasWebflowConstants.STATE_ID_VIEW_LOGIN_FORM);
    }
}

