package org.apereo.cas.web.flow;

/**
 * This is {@link CasWebflowConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowConstants {
    /**
     * The transition state 'success'.
     */
    String TRANSITION_ID_SUCCESS = "success";

    /** Proceed transition id. */
    String TRANSITION_ID_PROCEED = "proceed";

    /** Confirm transition id. */
    String TRANSITION_ID_CONFIRM = "confirm";

    /** Cancel transition id. */
    String TRANSITION_ID_CANCEL = "cancel";

    /** Enroll transition id. */
    String TRANSITION_ID_ENROLL = "enroll";
    
    /**
     * The state id 'success'.
     */
    String STATE_ID_SUCCESS = "success";

    /**
     * The state id 'verifyTrustedDevice'.
     */
    String STATE_ID_VERIFY_TRUSTED_DEVICE = "verifyTrustedDevice";

    /**
     * The view id 'registerDeviceView'.
     */
    String VIEW_ID_REGISTER_DEVICE = "registerDeviceView";

    /**
     * The state id 'registerTrustedDevice'.
     */
    String STATE_ID_REGISTER_TRUSTED_DEVICE = "registerTrustedDevice";

    /**
     * The state 'realSubmit'.
     */
    String STATE_ID_REAL_SUBMIT = "realSubmit";
    

    /**
     * The transition state 'successWithWarnings'.
     */
    String TRANSITION_ID_SUCCESS_WITH_WARNINGS = "successWithWarnings";

    /**
     * The decision state 'checkRegistrationRequired'.
     */
    String DECISION_STATE_REQUIRE_REGISTRATION = "checkRegistrationRequired";

    /**
     * The transition state 'yes'.
     */
    String TRANSITION_ID_YES = "yes";

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
     * The transition state 'gateway'.
     */
    String TRANSITION_ID_GATEWAY = "gateway";
    
    /**
     * The view state 'error'.
     */
    String VIEW_ID_ERROR = "error";

    /**
     * The view state 'showAuthenticationWarningMessages'.
     */
    String VIEW_ID_SHOW_AUTHN_WARNING_MSGS = "showAuthenticationWarningMessages";

    /**
     * The transition state 'authenticationFailure'.
     */
    String TRANSITION_ID_AUTHENTICATION_FAILURE = "authenticationFailure";

    /**
     * 'gateway' state id.
     */
    String STATE_ID_GATEWAY = "gateway";

    /**
     * The transition state 'warn'.
     */
    String TRANSITION_ID_WARN = "warn";

    /**
     * The transition state 'initialAuthenticationRequestValidationCheck'.
     */
    String STATE_ID_INITIAL_AUTHN_REQUEST_VALIDATION_CHECK = "initialAuthenticationRequestValidationCheck";

    /**
     * The transition state 'sendTicketGrantingTicket'.
     */
    String TRANSITION_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicket";

    /**
     * The state id 'sendTicketGrantingTicket'.
     */
    String STATE_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicket";

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
    String STATE_ID_REDIR_VIEW = "redirectView";

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
     * The flow var id 'credential'.
     */
    String VAR_ID_CREDENTIAL = "credential";

    /**
     * View id 'casResetPasswordSendInstructions'.
     */
    String VIEW_ID_SEND_RESET_PASSWORD_ACCT_INFO = "casResetPasswordSendInstructionsView";

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
     * Transition id 'resetPassword'.
     */
    String TRANSITION_ID_RESET_PASSWORD = "resetPassword";

    /**
     * State id 'doLogout'.
     */
    String STATE_ID_DO_LOGOUT = "doLogout";

    /**
     * State id 'finishLogout'.
     */
    String STATE_ID_FINISH_LOGOUT = "finishLogout";

    /**
     * State id 'casPasswordUpdateSuccess'.
     */
    String STATE_ID_PASSWORD_UPDATE_SUCCESS = "casPasswordUpdateSuccess";

    /**
     * State id 'handleAuthenticationFailure'.
     */
    String STATE_ID_HANDLE_AUTHN_FAILURE = "handleAuthenticationFailure";

    /**
     * Action id 'initialFlowSetupAction'.
     */
    String ACTION_ID_INIT_FLOW_SETUP = "initialFlowSetupAction";

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
     * Action id 'authenticationExceptionHandler'.
     */
    String ACTION_ID_AUTHENTICATION_EXCEPTION_HANDLER = "authenticationExceptionHandler";
    
    /**
     * Action id 'sendTicketGrantingTicketAction'.
     */
    String ACTION_ID_SEND_TICKET_GRANTING_TICKET = "sendTicketGrantingTicketAction";
    
    /**
     * Transition id 'changePassword'.
     */
    String TRANSITION_ID_CHANGE_PASSWORD = "changePassword";

    /**
     * State id 'proceedFromAuthenticationWarningView'.
     */
    String STATE_ID_PROCEED_FROM_AUTHENTICATION_WARNINGS_VIEW = "proceedFromAuthenticationWarningView";

    /**
     * Action to check if login should redirect to password reset subflow.
     */
    String CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION = "checkForPswdResetToken";

    /**
     * State for password reset subflow "pswdResetSubflow".
     */
    String STATE_ID_PASSWORD_RESET_SUBFLOW = "pswdResetSubflow";

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
    String STATE_ID_PSWD_CHANGE_CHECK_POST_LOGIN = "postLoginPswdChangeCheck";

    /**
     * State id to check for do change password manual flag "checkDoChangePassword".
     */
    String STATE_ID_CHECK_DO_CHANGE_PASSWORD = "checkDoChangePassword";
}
