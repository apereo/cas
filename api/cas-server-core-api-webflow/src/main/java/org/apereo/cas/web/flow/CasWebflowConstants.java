package org.apereo.cas.web.flow;

/**
 * This is {@link CasWebflowConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowConstants {

    /**
     * Base path for webflow configuration files.
     */
    String BASE_CLASSPATH_WEBFLOW = "classpath*:/webflow";

    /*
     ****************************************
     * Transitions.
     ****************************************
     */
    /**
     * The transition state 'authenticationFailure'.
     */
    String TRANSITION_ID_AUTHENTICATION_FAILURE = "authenticationFailure";

    /**
     * The transition state 'yes'.
     */
    String TRANSITION_ID_YES = "yes";

    /**
     * The transition state 'finalize'.
     */
    String TRANSITION_ID_FINALIZE = "finalize";

    /**
     * The transition state 'warn'.
     */
    String TRANSITION_ID_WARN = "warn";

    /**
     * The transition state 'no'.
     */
    String TRANSITION_ID_NO = "no";

    /**
     * The transition state 'submit'.
     */
    String TRANSITION_ID_SUBMIT = "submit";

    /**
     * The transition state 'error'.
     */
    String TRANSITION_ID_ERROR = "error";

    /**
     * The transition state 'resume'.
     */
    String TRANSITION_ID_RESUME = "resume";

    /**
     * The transition state 'gateway'.
     */
    String TRANSITION_ID_GATEWAY = "gateway";

    /**
     * The transition state 'stop'.
     */
    String TRANSITION_ID_STOP = "stop";

    /**
     * TGT does not exist event ID={@value}.
     **/
    String TRANSITION_ID_TGT_NOT_EXISTS = "notExists";

    /**
     * TGT invalid event ID={@value}.
     **/
    String TRANSITION_ID_TGT_INVALID = "invalid";

    /**
     * TGT valid event ID={@value}.
     **/
    String TRANSITION_ID_TGT_VALID = "valid";

    /**
     * The transition state 'interruptSkipped'.
     */
    String TRANSITION_ID_INTERRUPT_SKIPPED = "interruptSkipped";

    /**
     * The transition state 'interruptRequired'.
     */
    String TRANSITION_ID_INTERRUPT_REQUIRED = "interruptRequired";

    /**
     * Provider service is unavailable.
     */
    String TRANSITION_ID_UNAVAILABLE = "unavailable";

    /**
     * User allowed to bypass auth by provider.
     */
    String TRANSITION_ID_BYPASS = "bypass";

    /**
     * User was denied access by provider.
     */
    String TRANSITION_ID_DENY = "deny";

    /**
     * Transition id 'register'.
     */
    String TRANSITION_ID_REGISTER = "register";

    /**
     * The transition state 'success'.
     */
    String TRANSITION_ID_SUCCESS = "success";

    /**
     * Transition id 'redirect' .
     */
    String TRANSITION_ID_REDIRECT = "redirect";

    /**
     * Transition id 'skip' .
     */
    String TRANSITION_ID_SKIP = "skip";

    /**
     * Transition id 'approve' .
     */
    String TRANSITION_ID_APPROVE = "approve";

    /**
     * Propagate transition id.
     */
    String TRANSITION_ID_PROPAGATE = "propagate";

    /**
     * Finish transition id.
     */
    String TRANSITION_ID_FINISH = "finish";

    /**
     * Front transition id.
     */
    String TRANSITION_ID_FRONT = "front";

    /**
     * Proceed transition id.
     */
    String TRANSITION_ID_PROCEED = "proceed";

    /**
     * Confirm transition id.
     */
    String TRANSITION_ID_CONFIRM = "confirm";

    /**
     * Cancel transition id.
     */
    String TRANSITION_ID_CANCEL = "cancel";

    /**
     * Enroll transition id.
     */
    String TRANSITION_ID_ENROLL = "enroll";

    /**
     * Renew transition id.
     */
    String TRANSITION_ID_RENEW = "renew";

    /**
     * The transition state 'successWithWarnings'.
     */
    String TRANSITION_ID_SUCCESS_WITH_WARNINGS = "successWithWarnings";

    /**
     * Transition id 'resetPassword'.
     */
    String TRANSITION_ID_RESET_PASSWORD = "resetPassword";

    /**
     * Transition id 'forgotUsername'.
     */
    String TRANSITION_ID_FORGOT_USERNAME = "forgotUsername";

    /**
     * Transition id 'mustAcceptUsagePolicy'.
     */
    String TRANSITION_ID_AUP_MUST_ACCEPT = "mustAcceptUsagePolicy";

    /**
     * Transition id 'acceptedUsagePolicy'.
     */
    String TRANSITION_ID_AUP_ACCEPTED = "acceptedUsagePolicy";

    /**
     * State to determine the MFA failure mode and what action to take.
     */
    String TRANSITION_ID_MFA_FAILURE = "mfaFailure";

    /*
     ****************************************
     * States.
     ****************************************
     */
    /**
     * The state id 'success'.
     */
    String STATE_ID_SUCCESS = "success";

    /**
     * The state id 'stopWebflow'.
     */
    String STATE_ID_STOP_WEBFLOW = "stopWebflow";

    /**
     * The state id 'verifyTrustedDevice'.
     */
    String STATE_ID_VERIFY_TRUSTED_DEVICE = "verifyTrustedDevice";

    /**
     * The state id 'registerTrustedDevice'.
     */
    String STATE_ID_REGISTER_TRUSTED_DEVICE = "registerTrustedDevice";

    /**
     * The state 'realSubmit'.
     */
    String STATE_ID_REAL_SUBMIT = "realSubmit";

    /**
     * 'gateway' state id.
     */
    String STATE_ID_GATEWAY = "gateway";

    /**
     * 'finishMfaTrustedAuth' state id.
     */
    String STATE_ID_FINISH_MFA_TRUSTED_AUTH = "finishMfaTrustedAuth";

    /**
     * The transition state 'initialAuthenticationRequestValidationCheck'.
     */
    String STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK = "initialAuthenticationRequestValidationCheck";

    /**
     * The state id 'sendTicketGrantingTicket'.
     */
    String STATE_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicket";

    /**
     * The state id 'ticketGrantingTicketCheck'.
     */
    String STATE_ID_TICKET_GRANTING_TICKET_CHECK = "ticketGrantingTicketCheck";

    /**
     * The state id 'createTicketGrantingTicket'.
     */
    String STATE_ID_CREATE_TICKET_GRANTING_TICKET = "createTicketGrantingTicket";

    /**
     * The state 'initializeLoginForm'.
     */
    String STATE_ID_INIT_LOGIN_FORM = "initializeLoginForm";

    /**
     * The state 'viewLoginForm'.
     */
    String STATE_ID_VIEW_LOGIN_FORM = "viewLoginForm";

    /**
     * The state 'serviceAuthorizationCheck'.
     */
    String STATE_ID_SERVICE_AUTHZ_CHECK = "serviceAuthorizationCheck";

    /**
     * The state 'terminateSession'.
     */
    String STATE_ID_TERMINATE_SESSION = "terminateSession";

    /**
     * The state 'gatewayRequestCheck'.
     */
    String STATE_ID_GATEWAY_REQUEST_CHECK = "gatewayRequestCheck";

    /**
     * The state 'gatewayRequestCheck'.
     */
    String STATE_ID_GENERATE_SERVICE_TICKET = "generateServiceTicket";

    /**
     * The state 'gatewayServicesManagementCheck'.
     */
    String STATE_ID_GATEWAY_SERVICES_MGMT_CHECK = "gatewayServicesManagementCheck";

    /**
     * The state 'postView'.
     */
    String STATE_ID_POST_VIEW = "postView";

    /**
     * The state 'headerView'.
     */
    String STATE_ID_HEADER_VIEW = "headerView";

    /**
     * The state 'showWarningView'.
     */
    String STATE_ID_SHOW_WARNING_VIEW = "showWarningView";

    /**
     * The state 'redirectView'.
     */
    String STATE_ID_REDIRECT_VIEW = "redirectView";

    /**
     * The state id 'endWebflowExecution'.
     */
    String STATE_ID_END_WEBFLOW = "endWebflowExecution";

    /**
     * The state 'viewRedirectToUnauthorizedUrlView'.
     */
    String STATE_ID_VIEW_REDIR_UNAUTHZ_URL = "viewRedirectToUnauthorizedUrlView";

    /**
     * The state 'serviceUnauthorizedCheck'.
     */
    String STATE_ID_SERVICE_UNAUTHZ_CHECK = "serviceUnauthorizedCheck";

    /**
     * The state 'viewGenericLoginSuccess'.
     */
    String STATE_ID_VIEW_GENERIC_LOGIN_SUCCESS = "viewGenericLoginSuccess";

    /**
     * The state 'serviceCheck'.
     */
    String STATE_ID_SERVICE_CHECK = "serviceCheck";

    /**
     * The state 'viewServiceErrorView'.
     */
    String STATE_ID_VIEW_SERVICE_ERROR = "viewServiceErrorView";

    /**
     * The state id 'warn'.
     */
    String STATE_ID_WARN = "warn";

    /**
     * The state id 'renewRequestCheck'.
     */
    String STATE_ID_RENEW_REQUEST_CHECK = "renewRequestCheck";

    /**
     * The state id 'hasServiceCheck'.
     */
    String STATE_ID_HAS_SERVICE_CHECK = "hasServiceCheck";

    /**
     * The state id 'redirect'.
     */
    String STATE_ID_REDIRECT = "redirect";

    /**
     * State id when MFA provider has been detected as unavailable and failureMode is closed.
     */
    String STATE_ID_MFA_UNAVAILABLE = "mfaUnavailable";

    /**
     * State id when MFA provider has denied access to a user because of account lockout.
     */
    String STATE_ID_MFA_DENIED = "mfaDenied";

    /**
     * State id 'doLogout'.
     */
    String STATE_ID_DO_LOGOUT = "doLogout";

    /**
     * State id 'propagateLogoutRequests'.
     */
    String STATE_ID_PROPAGATE_LOGOUT_REQUESTS = "propagateLogoutRequests";

    /**
     * State id 'logoutView'.
     */
    String STATE_ID_LOGOUT_VIEW = "logoutView";

    /**
     * State id 'finishLogout'.
     */
    String STATE_ID_FINISH_LOGOUT = "finishLogout";

    /**
     * Delegated authentication state id.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION = "delegatedAuthenticationAction";

    /**
     * State id 'frontLogout'.
     */
    String STATE_ID_FRONT_LOGOUT = "frontLogout";

    /**
     * State id 'confirmLogoutView'.
     */
    String STATE_ID_CONFIRM_LOGOUT_VIEW = "confirmLogoutView";

    /**
     * State id 'casPasswordUpdateSuccess'.
     */
    String STATE_ID_PASSWORD_UPDATE_SUCCESS = "casPasswordUpdateSuccess";

    /**
     * State id 'handleAuthenticationFailure'.
     */
    String STATE_ID_HANDLE_AUTHN_FAILURE = "handleAuthenticationFailure";

    /**
     * State for password reset subflow "pswdResetSubflow".
     */
    String STATE_ID_PASSWORD_RESET_SUBFLOW = "pswdResetSubflow";

    /**
     * State id 'proceedFromAuthenticationWarningView'.
     */
    String STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW = "proceedFromAuthenticationWarningView";

    /**
     * Login flow state indicating the password reset subflow is complete "pswdResetComplete".
     */
    String STATE_ID_PASSWORD_RESET_FLOW_COMPLETE = "pswdResetComplete";

    /**
     * State to restart the login flow fresh "redirectToLogin".
     */
    String STATE_ID_REDIRECT_TO_LOGIN = "redirectToLogin";

    /**
     * State to check where the password change should go after completion (post or pre-login) "postLoginPswdChangeCheck".
     */
    String STATE_ID_POST_LOGIN_PASSWORD_CHANGE_CHECK = "postLoginPswdChangeCheck";

    /**
     * State id to check for do change password manual flag "checkDoChangePassword".
     */
    String STATE_ID_CHECK_DO_CHANGE_PASSWORD = "checkDoChangePassword";

    /**
     * State to check if the MFA provider is available.
     */
    String STATE_ID_MFA_CHECK_AVAILABLE = "mfaCheckAvailable";

    /**
     * State to check if the MFA provider should be bypassed.
     */
    String STATE_ID_MFA_CHECK_BYPASS = "mfaCheckBypass";

    /**
     * State that can be used by MFA providers that offer preAuth endpoints.
     */
    String STATE_ID_MFA_PRE_AUTH = "mfaPreAuth";

    /**
     * The view state 'showAuthenticationWarningMessages'.
     */
    String STATE_ID_SHOW_AUTHN_WARNING_MSGS = "showAuthenticationWarningMessages";
    /*
     ****************************************
     * Views.
     ****************************************
     */
    /**
     * The view id 'registerDeviceView'.
     */
    String VIEW_ID_REGISTER_DEVICE = "registerDeviceView";

    /**
     * The view state 'casPac4jStopWebflow'.
     */
    String VIEW_ID_PAC4J_STOP_WEBFLOW = "casPac4jStopWebflow";

    /**
     * The view state 'casWsFedStopWebflow'.
     */
    String VIEW_ID_WSFED_STOP_WEBFLOW = "casWsFedStopWebflow";

    /**
     * The view state 'error'.
     */
    String VIEW_ID_ERROR = "error";

    /**
     * View id when MFA provider has been detected as unavailable and failureMode is closed.
     */
    String VIEW_ID_MFA_UNAVAILABLE = "casMfaUnavailableView";

    /**
     * View id when MFA provider has denied access to a user because of account lockout.
     */
    String VIEW_ID_MFA_DENIED = "casMfaDeniedView";

    /**
     * The view id 'casPostResponseView'.
     */
    String VIEW_ID_POST_RESPONSE = "casPostResponseView";

    /**
     * The view id 'casGenericSuccessView'.
     */
    String VIEW_ID_GENERIC_SUCCESS = "casGenericSuccessView";

    /**
     * The view id 'casConfirmView'.
     */
    String VIEW_ID_CONFIRM = "casConfirmView";

    /**
     * The view id 'casServiceErrorView'.
     */
    String VIEW_ID_SERVICE_ERROR = "casServiceErrorView";

    /**
     * View id 'casResetPasswordSendInstructions'.
     */
    String VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO = "casResetPasswordSendInstructionsView";

    /**
     * View id 'casForgotUsernameSentInfoView'.
     */
    String VIEW_ID_SENT_FORGOT_USERNAME_ACCT_INFO = "casForgotUsernameSentInfoView";

    /**
     * View id 'casForgotUsernameSendInfoView'.
     */
    String VIEW_ID_FORGOT_USERNAME_ACCT_INFO = "casForgotUsernameSendInfoView";

    /**
     * View id 'casBadHoursView'.
     */
    String VIEW_ID_INVALID_AUTHENTICATION_HOURS = "casBadHoursView";

    /**
     * View id 'casPasswordUpdateSuccessView'.
     */
    String VIEW_ID_PASSWORD_UPDATE_SUCCESS = "casPasswordUpdateSuccessView";

    /**
     * View id 'casAuthenticationBlockedView'.
     */
    String VIEW_ID_AUTHENTICATION_BLOCKED = "casAuthenticationBlockedView";

    /**
     * View id 'casResetPasswordErrorView'.
     */
    String VIEW_ID_PASSWORD_RESET_ERROR = "casResetPasswordErrorView";

    /**
     * View id 'casBadWorkstationView'.
     */
    String VIEW_ID_INVALID_WORKSTATION = "casBadWorkstationView";

    /**
     * View id 'casAccountDisabledView'.
     */
    String VIEW_ID_ACCOUNT_DISABLED = "casAccountDisabledView";

    /**
     * View id 'casAccountLockedView'.
     */
    String VIEW_ID_ACCOUNT_LOCKED = "casAccountLockedView";

    /**
     * View id 'casMustChangePassView'.
     */
    String VIEW_ID_MUST_CHANGE_PASSWORD = "casMustChangePassView";

    /**
     * View id 'casExpiredPassView'.
     */
    String VIEW_ID_EXPIRED_PASSWORD = "casExpiredPassView";

    /**
     * View id 'casResetPasswordSentInstructions'.
     */
    String VIEW_ID_SENT_RESET_PASSWORD_ACCT_INFO = "casResetPasswordSentInstructionsView";

    /**
     * View name used for form-login into admin/actuator endpoints.
     */
    String VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW = "casAdminLoginView";
    /*
     ****************************************
     * Decisions.
     ****************************************
     */

    /**
     * The decision state 'checkRegistrationRequired'.
     */
    String DECISION_STATE_REQUIRE_REGISTRATION = "checkRegistrationRequired";

    /**
     * The decision state 'finishLogout'.
     */
    String DECISION_STATE_FINISH_LOGOUT = "finishLogout";

    /**
     * Action to check if login should redirect to password reset subflow.
     */
    String DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION = "checkForPswdResetToken";

    /*
     ****************************************
     * Variables & Attributes.
     ****************************************
     */

    /**
     * The flow var id 'credential'.
     */
    String VAR_ID_CREDENTIAL = "credential";

    /**
     * The flow var id 'providerId'.
     */
    String VAR_ID_MFA_PROVIDER_ID = "mfaProviderId";


    /**
     * Event attribute id 'authenticationWarnings'.
     */
    String ATTRIBUTE_ID_AUTHENTICATION_WARNINGS = "authenticationWarnings";

    /*
     ****************************************
     * Actions.
     ****************************************
     */
    /**
     * Action id 'renderLoginFormAction'.
     */
    String ACTION_ID_RENDER_LOGIN_FORM = "renderLoginFormAction";

    /**
     * Action id 'authenticationViaFormAction'.
     */
    String ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION = "authenticationViaFormAction";

    /**
     * Action id 'compositeMfaProviderSelectedAction'.
     */
    String ACTION_ID_MFA_PROVIDER_SELECTED = "compositeMfaProviderSelectedAction";

    /**
     * Action id 'initialFlowSetupAction'.
     */
    String ACTION_ID_INIT_FLOW_SETUP = "initialFlowSetupAction";
    /**
     * Action id 'verifyRequiredServiceAction'.
     */
    String ACTION_ID_VERIFY_REQUIRED_SERVICE = "verifyRequiredServiceAction";

    /**
     * Action id 'ticketGrantingTicketCheckAction'.
     */
    String ACTION_ID_TICKET_GRANTING_TICKET_CHECK = "ticketGrantingTicketCheckAction";

    /**
     * Action id 'initialAuthenticationRequestValidationAction'.
     */
    String ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION = "initialAuthenticationRequestValidationAction";

    /**
     * Action id 'remoteAuthenticate'.
     */
    String ACTION_ID_REMOTE_TRUSTED_AUTHENTICATION = "remoteAuthenticate";

    /**
     * Action id 'clearWebflowCredentialsAction'.
     */
    String ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS = "clearWebflowCredentialsAction";

    /**
     * Action id 'generateServiceTicketAction'.
     */
    String ACTION_ID_GENERATE_SERVICE_TICKET = "generateServiceTicketAction";

    /**
     * Action id 'redirectToServiceAction'.
     */
    String ACTION_ID_REDIRECT_TO_SERVICE = "redirectToServiceAction";

    /**
     * Action id 'redirectToServiceAction'.
     */
    String ACTION_ID_TERMINATE_SESSION = "terminateSessionAction";

    /**
     * Action id 'logoutViewSetupAction'.
     */
    String ACTION_ID_LOGOUT_VIEW_SETUP = "logoutViewSetupAction";

    /**
     * Action id 'authenticationExceptionHandler'.
     */
    String ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER = "authenticationExceptionHandler";

    /**
     * Action id 'sendTicketGrantingTicketAction'.
     */
    String ACTION_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicketAction";

    /**
     * Action id 'createTicketGrantingTicketAction'.
     */
    String ACTION_ID_CREATE_TICKET_GRANTING_TICKET = "createTicketGrantingTicketAction";

    /**
     * Action id `delegatedAuthenticationAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION = "delegatedAuthenticationAction";

    /**
     * Action id `renewAuthenticationRequestCheckAction`.
     */
    String ACTION_ID_RENEW_AUTHN_REQUEST = "renewAuthenticationRequestCheckAction";
}
