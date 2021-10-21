package org.apereo.cas.web.flow;

/**
 * This is {@link CasWebflowConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface CasWebflowConstants {
    /*
     ****************************************
     * Bean Names.
     ****************************************
     */

    /**
     * Bean name for login flow registry.
     */
    String BEAN_NAME_LOGIN_FLOW_DEFINITION_REGISTRY = "loginFlowRegistry";
    /**
     * Bean name for logout flow registry.
     */
    String BEAN_NAME_LOGOUT_FLOW_DEFINITION_REGISTRY = "logoutFlowRegistry";
    /**
     * Bean name for flow builder services.
     */
    String BEAN_NAME_FLOW_BUILDER_SERVICES = "flowBuilderServices";
    /**
     * Bean name for flow builder.
     */
    String BEAN_NAME_FLOW_BUILDER = "flowBuilder";

    /*
     ****************************************
     * Errors.
     ****************************************
     */

    /**
     * Attribute to track exceptions in models.
     */
    String ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION = "rootCauseException";

    /**
     * Attribute to track registered service in the flow.
     */
    String ATTRIBUTE_REGISTERED_SERVICE = "registeredService";

    /**
     * Attribute to track service in the flow.
     */
    String ATTRIBUTE_SERVICE = "service";

    /*
     ****************************************
     * Transitions.
     ****************************************
     */

    /**
     * The transition state 'discovery'.
     */
    String TRANSITION_ID_DISCOVERY = "discovery";

    /**
     * The transition state 'execute'.
     */
    String TRANSITION_ID_EXECUTE = "execute";

    /**
     * The transition state 'back'.
     */
    String TRANSITION_ID_BACK = "back";

    /**
     * The transition state 'captchaError'.
     */
    String TRANSITION_ID_CAPTCHA_ERROR = "captchaError";

    /**
     * The transition state 'authenticationFailure'.
     */
    String TRANSITION_ID_AUTHENTICATION_FAILURE = "authenticationFailure";

    /**
     * The transition state 'yes'.
     */
    String TRANSITION_ID_YES = "yes";

    /**
     * The transition state 'prompt'.
     */
    String TRANSITION_ID_PROMPT = "prompt";

    /**
     * The transition state 'finalize'.
     */
    String TRANSITION_ID_FINALIZE = "finalize";

    /**
     * The view id 'surrogateListView'.
     */
    String TRANSITION_ID_SURROGATE_VIEW = "surrogateListView";

    /**
     * Skip surrogate view if no surrogates can be found.
     */
    String TRANSITION_ID_SKIP_SURROGATE = "skipSurrogateView";

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
     * The transition state 'resend'.
     */
    String TRANSITION_ID_RESEND = "resend";

    /**
     * The transition state 'error'.
     */
    String TRANSITION_ID_ERROR = "error";

    /**
     * The transition state 'validate'.
     */
    String TRANSITION_ID_VALIDATE = "validate";

    /**
     * The transition state 'resume'.
     */
    String TRANSITION_ID_RESUME = "resume";

    /**
     * Transition id `retry`.
     */
    String TRANSITION_ID_RETRY = "retry";

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
    String TRANSITION_ID_TICKET_GRANTING_TICKET_NOT_EXISTS = "notExists";

    /**
     * TGT invalid event ID={@value}.
     **/
    String TRANSITION_ID_TICKET_GRANTING_TICKET_INVALID = "invalid";

    /**
     * TGT valid event ID={@value}.
     **/
    String TRANSITION_ID_TICKET_GRANTING_TICKET_VALID = "valid";

    /**
     * Transition id to generate service tickets.
     */
    String TRANSITION_ID_GENERATE_SERVICE_TICKET = "generateServiceTicket";

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
     * Transition id 'delete'.
     */
    String TRANSITION_ID_DELETE = "delete";
    /**
     * Transition id 'store'.
     */
    String TRANSITION_ID_STORE = "store";

    /**
     * Transition id 'select'.
     */
    String TRANSITION_ID_SELECT = "select";

    /**
     * The transition state 'success'.
     */
    String TRANSITION_ID_SUCCESS = "success";

    /**
     * Transition id 'redirect' .
     */
    String TRANSITION_ID_REDIRECT = "redirect";

    /**
     * Transition id 'post' .
     */
    String TRANSITION_ID_POST = "post";

    /**
     * Transition id 'mfa-composite'.
     */
    String TRANSITION_ID_MFA_COMPOSITE = "mfa-composite";

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
     * Signup transition id.
     */
    String TRANSITION_ID_SIGNUP = "signup";

    /**
     * Renew transition id.
     */
    String TRANSITION_ID_RENEW = "renew";

    /**
     * The transition state 'successWithWarnings'.
     */
    String TRANSITION_ID_SUCCESS_WITH_WARNINGS = "successWithWarnings";

    /**
     * Transition id 'passwordUpdateSuccess'.
     */
    String TRANSITION_ID_PASSWORD_UPDATE_SUCCESS = "passwordUpdateSuccess";

    /**
     * Transition id 'resetPassword'.
     */
    String TRANSITION_ID_RESET_PASSWORD = "resetPassword";
    /**
     * Transition id 'invalidPasswordResetToken'.
     */
    String TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN = "invalidPasswordResetToken";

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
     * The state id 'finishedInterrupt'.
     */
    String STATE_ID_FINISHED_INTERRUPT = "finishedInterrupt";

    /**
     * The state id 'inquireInterruptAction'.
     */
    String STATE_ID_INQUIRE_INTERRUPT_ACTION = "inquireInterruptAction";

    /**
     * The state id 'finalizeInterruptFlowAction'.
     */
    String STATE_ID_FINALIZE_INTERRUPT_ACTION = "finalizeInterruptFlowAction";

    /**
     * The state id 'prepareRegisterTrustedDevice'.
     */
    String STATE_ID_PREPARE_REGISTER_TRUSTED_DEVICE = "prepareRegisterTrustedDevice";

    /**
     * The state 'realSubmit'.
     */
    String STATE_ID_REAL_SUBMIT = "realSubmit";

    /**
     * 'finishMfaTrustedAuth' state id.
     */
    String STATE_ID_FINISH_MFA_TRUSTED_AUTH = "finishMfaTrustedAuth";

    /**
     * 'getSecurityQuestionsView' state id.
     */
    String STATE_ID_SECURITY_QUESTIONS_VIEW = "getSecurityQuestionsView";


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
     * The state id 'startX509Authenticate'.
     */
    String STATE_ID_X509_START = "startX509Authenticate";

    /**
     * The state id 'createTicketGrantingTicket'.
     */
    String STATE_ID_CREATE_TICKET_GRANTING_TICKET = "createTicketGrantingTicket";

    /**
     * The state 'initializeLoginForm'.
     */
    String STATE_ID_INIT_LOGIN_FORM = "initializeLoginForm";

    /**
     * The state 'afterInitializeLoginForm'.
     */
    String STATE_ID_AFTER_INIT_LOGIN_FORM = "afterInitializeLoginForm";

    /**
     * The state 'cancel'.
     */
    String STATE_ID_CANCEL = "cancel";

    /**
     * The state 'surrogateListView'.
     */
    String STATE_ID_SURROGATE_VIEW = "surrogateListView";

    /**
     * The state 'loadSurrogatesAction'.
     */
    String STATE_ID_LOAD_SURROGATES_ACTION = "loadSurrogatesAction";

    /**
     * The state 'selectSurrogate'.
     */
    String STATE_ID_SELECT_SURROGATE = "selectSurrogate";

    /**
     * The state 'duoUniversalPromptPrepareValidate'.
     */
    String STATE_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN = "duoUniversalPromptPrepareValidate";

    /**
     * The state 'viewAccountSignUp'.
     */
    String STATE_ID_VIEW_ACCOUNT_SIGNUP = "viewAccountSignUp";

    /**
     * The state 'completeAccountRegistrationView'.
     */
    String STATE_ID_COMPLETE_ACCOUNT_REGISTRATION = "completeAccountRegistrationView";

    /**
     * The state 'accountRegistrationSubflow'.
     */
    String STATE_ID_ACCOUNT_REGISTRATION_SUBFLOW = "accountRegistrationSubflow";

    /**
     * The state 'accountSignUpInfoSent'.
     */
    String STATE_ID_SENT_ACCOUNT_SIGNUP_INFO = "accountSignUpInfoSent";

    /**
     * The state 'submitAccountRegistration'.
     */
    String STATE_ID_SUBMIT_ACCOUNT_REGISTRATION = "submitAccountRegistration";

    /**
     * The state 'viewLoginForm'.
     */
    String STATE_ID_VIEW_LOGIN_FORM = "viewLoginForm";

    /**
     * The state 'unavailable'.
     */
    String STATE_ID_UNAVAILABLE = "unavailable";

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
     * The state 'generateServiceTicket'.
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
     * The state 'mfaFailure'.
     */
    String STATE_ID_MFA_FAILURE = "mfaFailure";

    /**
     * The state 'deny'.
     */
    String STATE_ID_DENY = "deny";

    /**
     * The state 'finalizeWarning'.
     */
    String STATE_ID_FINALIZE_WARNING = "finalizeWarning";

    /**
     * The state 'registerDevice'.
     */
    String STATE_ID_REGISTER_DEVICE = "registerDevice";

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
     * The state 'viewWebflowConfigurationErrorView'.
     */
    String STATE_ID_VIEW_WEBFLOW_CONFIG_ERROR = "viewWebflowConfigurationErrorView";

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
     * State id 'acceptableUsagePolicyView.
     */
    String STATE_ID_ACCEPTABLE_USAGE_POLICY_VIEW = "acceptableUsagePolicyView";

    /**
     * State id 'aupAcceptedAction.
     */
    String STATE_ID_AUP_ACCEPTED = "aupAcceptedAction";

    /**
     * State id 'acceptableUsagePolicyCheck.
     */
    String STATE_ID_AUP_CHECK = "acceptableUsagePolicyCheck";

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
     * State id 'redirectToDelegatedAuthnProviderView'.
     */
    String STATE_ID_REDIRECT_TO_DELEGATED_AUTHN_PROVIDER_VIEW = "redirectToDelegatedAuthnProviderView";

    /**
     * State id 'delegatedAuthenticationDynamicDiscoveryView'.
     */
    String STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_VIEW = "delegatedAuthenticationDynamicDiscoveryView";

    /**
     * State id 'delegatedAuthenticationProviderDiscoveryExecution'.
     */
    String STATE_ID_DELEGATED_AUTHN_DYNAMIC_DISCOVERY_EXECUTION = "delegatedAuthenticationProviderDiscoveryExecution";

    /**
     * Delegated authentication state id.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION = "delegatedAuthentication";

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
     * State if for MFA composite events.
     */
    String STATE_ID_MFA_COMPOSITE = "mfa-composite";

    /**
     * State that can be used by MFA providers that offer preAuth endpoints.
     */
    String STATE_ID_MFA_PRE_AUTH = "mfaPreAuth";

    /**
     * The view state 'showAuthenticationWarningMessages'.
     */
    String STATE_ID_SHOW_AUTHN_WARNING_MSGS = "showAuthenticationWarningMessages";

    /**
     * State id 'openIdSingleSignOnAction'.
     */
    String STATE_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION = "openIdSingleSignOnAction";

    /**
     * The state id 'registerDeviceView'.
     */
    String STATE_ID_REGISTER_DEVICE_VIEW = "registerDeviceView";

    /**
     * State id 'verifyPasswordResetRequest'.
     */
    String STATE_ID_VERIFY_PASSWORD_RESET_REQUEST = "verifyPasswordResetRequest";

    /**
     * The state id 'spnego'.
     */
    String STATE_ID_SPNEGO = "spnego";

    /**
     * The state id 'interruptView'.
     */
    String STATE_ID_INTERRUPT_VIEW = "interruptView";

    /**
     * The state id 'startSpnegoAuthenticate'.
     */
    String STATE_ID_START_SPNEGO_AUTHENTICATE = "startSpnegoAuthenticate";

    /**
     * The state id 'evaluateClientRequest'.
     */
    String STATE_ID_EVALUATE_SPNEGO_CLIENT = "evaluateClientRequest";

    /**
     * The state state 'sendPasswordResetInstructions'.
     */
    String STATE_ID_SEND_PASSWORD_RESET_INSTRUCTIONS = "sendPasswordResetInstructions";

    /**
     * The state state 'sendForgotUsernameInstructions'.
     */
    String STATE_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS = "sendForgotUsernameInstructions";

    /**
     * The view state 'passwordChangeAction'.
     */
    String STATE_ID_PASSWORD_CHANGE_ACTION = "passwordChangeAction";

    /**
     * The state 'passwordResetErrorView'.
     */
    String STATE_ID_PASSWORD_RESET_ERROR_VIEW = "passwordResetErrorView";

    /**
     * State id `delegatedAuthenticationClientRetry`.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY = "delegatedAuthenticationClientRetry";

    /**
     * View id 'casBadWorkstationView'.
     */
    String STATE_ID_INVALID_WORKSTATION = "casBadWorkstationView";

    /**
     * State id 'casAccountDisabledView'.
     */
    String STATE_ID_ACCOUNT_DISABLED = "casAccountDisabledView";

    /**
     * State id 'casAccountLockedView'.
     */
    String STATE_ID_ACCOUNT_LOCKED = "casAccountLockedView";

    /**
     * State id 'casBadHoursView'.
     */
    String STATE_ID_INVALID_AUTHENTICATION_HOURS = "casBadHoursView";

    /**
     * State id 'casAuthenticationBlockedView'.
     */
    String STATE_ID_AUTHENTICATION_BLOCKED = "casAuthenticationBlockedView";

    /**
     * State id 'casMustChangePassView'.
     */
    String STATE_ID_MUST_CHANGE_PASSWORD = "casMustChangePassView";
    /**
     * State id 'verifySecurityQuestions'.
     */
    String STATE_ID_VERIFY_SECURITY_QUESTIONS = "verifySecurityQuestions";

    /**
     * State id 'casExpiredPassView'.
     */
    String STATE_ID_EXPIRED_PASSWORD = "casExpiredPassView";

    /**
     * State id 'casForgotUsernameSendInfoView'.
     */
    String STATE_ID_FORGOT_USERNAME_ACCT_INFO = "casForgotUsernameSendInfoView";

    /**
     * State id 'casResetPasswordSentInstructions'.
     */
    String STATE_ID_SENT_RESET_PASSWORD_ACCT_INFO = "casResetPasswordSentInstructionsView";

    /**
     * State id 'casResetPasswordSendInstructions'.
     */
    String STATE_ID_SEND_RESET_PASSWORD_ACCT_INFO = "casResetPasswordSendInstructionsView";

    /**
     * State id 'casForgotUsernameSentInfoView'.
     */
    String STATE_ID_SENT_FORGOT_USERNAME_ACCT_INFO = "casForgotUsernameSentInfoView";

    /**
     * State id 'accountRegistrationCheck'.
     */
    String STATE_ID_CHECK_ACCOUNT_REGISTRATION = "accountRegistrationCheck";

    /**
     * State id 'viewRegistration'.
     */
    String STATE_ID_VIEW_REGISTRATION = "viewRegistration";

    /**
     * State id 'saveRegistration'.
     */
    String STATE_ID_SAVE_REGISTRATION = "saveRegistration";

    /**
     * State id 'initPasswordReset'.
     */
    String STATE_ID_INIT_PASSWORD_RESET = "initPasswordReset";

    /**
     * State id 'compositeMfaProviderSelectedAction'.
     */
    String STATE_ID_MFA_PROVIDER_SELECTED = "compositeMfaProviderSelectedAction";
    /**
     * State id 'validateAccountRegistrationToken'.
     */
    String STATE_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN = "validateAccountRegistrationToken";

    /**
     * State id 'determineDuoRequest'.
     */
    String STATE_ID_DETERMINE_DUO_REQUEST = "determineDuoRequest";

    /**
     * State id 'viewLoginFormDuo'.
     */
    String STATE_ID_VIEW_LOGIN_FORM_DUO = "viewLoginFormDuo";

    /**
     * State id 'finalizeAuthentication'.
     */
    String STATE_ID_FINALIZE_AUTHENTICATION = "finalizeAuthentication";

    /**
     * State id 'determineDuoUserAccount'.
     */
    String STATE_ID_DETERMINE_DUO_USER_ACCOUNT = "determineDuoUserAccount";

    /**
     * State id 'duoNonWebAuthentication'.
     */
    String STATE_ID_DUO_NON_WEB_AUTHENTICATION = "duoNonWebAuthentication";

    /*
     ****************************************
     * Views.
     ****************************************
     */

    /**
     * The view state 'error'.
     */
    String VIEW_ID_ERROR = "error";

    /**
     * The view id 'casPostResponseView'.
     */
    String VIEW_ID_POST_RESPONSE = "casPostResponseView";

    /**
     * The view id 'casServiceErrorView'.
     */
    String VIEW_ID_SERVICE_ERROR = "error/casServiceErrorView";

    /**
     * The view id 'casWebflowConfigErrorView'.
     */
    String VIEW_ID_WEBFLOW_CONFIG_ERROR = "error/casWebflowConfigErrorView";

    /**
     * View name used for form-login into admin/actuator endpoints.
     */
    String VIEW_ID_ENDPOINT_ADMIN_LOGIN_VIEW = "admin/casAdminLoginView";

    /**
     * View id 'casDelegatedAuthnErrorView'.
     */
    String VIEW_ID_DELEGATED_AUTHN_ERROR_VIEW = "delegated-authn/casDelegatedAuthnErrorView";

    /**
     * The view state 'casPac4jStopWebflow'.
     */
    String VIEW_ID_PAC4J_STOP_WEBFLOW = "delegated-authn/casDelegatedAuthnStopWebflow";

    /**
     * The view state 'casSessionStorageWriteView'.
     */
    String VIEW_ID_SESSION_STORAGE_WRITE = "storage/casSessionStorageWriteView";

    /**
     * The view state 'casSessionStorageReadView'.
     */
    String VIEW_ID_SESSION_STORAGE_READ = "storage/casSessionStorageReadView";

    /*
     ****************************************
     * Decisions.
     ****************************************
     */

    /**
     * Action to check if login should redirect to password reset subflow.
     */
    String DECISION_STATE_CHECK_FOR_PASSWORD_RESET_TOKEN_ACTION = "checkForPswdResetToken";

    /**
     * Action state 'selectFirstAction'.
     */
    String DECISION_STATE_OPEN_ID_SELECT_FIRST_ACTION = "selectFirstAction";

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
     * The flow var id 'mfaTrustRecord'.
     */
    String VAR_ID_MFA_TRUST_RECORD = "mfaTrustRecord";

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
     * Action id 'delegatedAuthenticationProviderDynamicDiscoveryAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION = "delegatedAuthenticationProviderDynamicDiscoveryExecutionAction";

    /**
     * Action id 'multifactorProviderSelectedAction'.
     */
    String ACTION_ID_MULTIFACTOR_PROVIDER_SELECTED = "multifactorProviderSelectedAction";

    /**
     * Action id 'injectResponseHeadersAction'.
     */
    String ACTION_ID_INJECT_RESPONSE_HEADERS = "injectResponseHeadersAction";

    /**
     * Action id 'serviceAuthorizationCheck'.
     */
    String ACTION_ID_SERVICE_AUTHZ_CHECK = "serviceAuthorizationCheck";

    /**
     * Action id 'gatewayServicesManagementCheck'.
     */
    String ACTION_ID_GATEWAY_CHECK = "gatewayServicesManagementCheck";

    /**
     * Action id 'validateCaptchaAction'.
     */
    String ACTION_ID_VALIDATE_CAPTCHA = "validateCaptchaAction";

    /**
     * Action id 'x509Check'.
     */
    String ACTION_ID_X509_CHECK = "x509Check";

    /**
     * Action id 'initializeLoginAction'.
     */
    String ACTION_ID_INIT_LOGIN_ACTION = "initializeLoginAction";

    /**
     * Action id 'initializeCaptchaAction'.
     */
    String ACTION_ID_INIT_CAPTCHA = "initializeCaptchaAction";

    /**
     * Action id 'passwordResetValidateCaptchaAction'.
     */
    String ACTION_ID_PASSWORD_RESET_VALIDATE_CAPTCHA = "passwordResetValidateCaptchaAction";

    /**
     * Action id 'forgotUsernameValidateCaptchaAction'.
     */
    String ACTION_ID_FORGOT_USERNAME_VALIDATE_CAPTCHA = "forgotUsernameValidateCaptchaAction";

    /**
     * Action id 'passwordResetInitializeCaptchaAction'.
     */
    String ACTION_ID_PASSWORD_RESET_INIT_CAPTCHA = "passwordResetInitializeCaptchaAction";

    /**
     * Action id 'forgotUsernameInitializeCaptchaAction'.
     */
    String ACTION_ID_FORGOT_USERNAME_INIT_CAPTCHA = "forgotUsernameInitializeCaptchaAction";

    /**
     * Action id 'initPasswordChangeAction .
     */
    String ACTION_ID_INIT_PASSWORD_CHANGE = "initPasswordChangeAction";

    /**
     * Action id 'renderLoginFormAction'.
     */
    String ACTION_ID_RENDER_LOGIN_FORM = "renderLoginFormAction";

    /**
     * Action id 'authenticationViaFormAction'.
     */
    String ACTION_ID_AUTHENTICATION_VIA_FORM_ACTION = "authenticationViaFormAction";

    /**
     * Action id 'initialFlowSetupAction'.
     */
    String ACTION_ID_INITIAL_FLOW_SETUP = "initialFlowSetupAction";

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
     * Action id 'serviceWarningAction'.
     */
    String ACTION_ID_SERVICE_WARNING = "serviceWarningAction";

    /**
     * Action id 'finishLogoutAction'.
     */
    String ACTION_ID_FINISH_LOGOUT = "finishLogoutAction";

    /**
     * Action id 'populateSpringSecurityContextAction'.
     */
    String ACTION_ID_POPULATE_SECURITY_CONTEXT = "populateSpringSecurityContextAction";

    /**
     * Action id 'logoutAction'.
     */
    String ACTION_ID_LOGOUT = "logoutAction";

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
     * Action id `delegatedAuthenticationClientLogoutAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_LOGOUT = "delegatedAuthenticationClientLogoutAction";


    /**
     * Action id `delegatedAuthenticationClientFinishLogoutAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_FINISH_LOGOUT = "delegatedAuthenticationClientFinishLogoutAction";

    /**
     * Action id `delegatedAuthenticationClientFinishLogoutAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_RETRY = "delegatedAuthenticationClientRetryAction";

    /**
     * Action id `renewAuthenticationRequestCheckAction`.
     */
    String ACTION_ID_RENEW_AUTHN_REQUEST = "renewAuthenticationRequestCheckAction";

    /**
     * Action id 'openIdSingleSignOnAction .
     *
     * @deprecated Since 6.2
     */
    @Deprecated(since = "6.2.0")
    String ACTION_ID_OPEN_ID_SINGLE_SIGN_ON_ACTION = "openIdSingleSignOnAction";

    /**
     * Action id 'negociateSpneg .
     */
    String ACTION_ID_SPNEGO_NEGOTIATE = "negociateSpnego";

    /**
     * Action id 'acceptableUsagePolicyVerifyAction.
     */
    String ACTION_ID_AUP_VERIFY = "acceptableUsagePolicyVerifyAction";

    /**
     * Action id 'acceptableUsagePolicyRenderAction.
     */
    String ACTION_ID_AUP_RENDER = "acceptableUsagePolicyRenderAction";

    /**
     * Action id 'acceptableUsagePolicyVerifyServiceAction .
     */
    String ACTION_ID_AUP_VERIFY_SERVICE = "acceptableUsagePolicyVerifyServiceAction";

    /**
     * Action id 'sendForgotUsernameInstructionsAction .
     */
    String ACTION_ID_SEND_FORGOT_USERNAME_INSTRUCTIONS_ACTION = "sendForgotUsernameInstructionsAction";

    /**
     * Action id 'prepareInterruptViewAction .
     */
    String ACTION_ID_PREPARE_INTERRUPT_VIEW = "prepareInterruptViewAction";

    /**
     * Action id 'inquireInterruptAction .
     */
    String ACTION_ID_INQUIRE_INTERRUPT = "inquireInterruptAction";

    /**
     * Action id 'prepareMultifactorProviderSelectionAction'.
     */
    String ACTION_ID_PREPARE_MULTIFACTOR_PROVIDER_SELECTION = "prepareMultifactorProviderSelectionAction";

    /**
     * Action id 'finalizeInterruptFlowAction .
     */
    String ACTION_ID_FINALIZE_INTERRUPT = "finalizeInterruptFlowAction";

    /**
     * Action id 'oneTimeTokenAuthenticationWebflowAction .
     */
    String ACTION_ID_OTP_AUTHENTICATION_ACTION = "oneTimeTokenAuthenticationWebflowAction";

    /**
     * Action id 'loadSurrogatesListAction .
     */
    String ACTION_ID_LOAD_SURROGATES_LIST_ACTION = "loadSurrogatesListAction";

    /**
     * Action id 'selectSurrogateAction .
     */
    String ACTION_ID_SELECT_SURROGATE_ACTION = "selectSurrogateAction";

    /**
     * Action id 'duoUniversalPromptPrepareLoginAction .
     */
    String ACTION_ID_DUO_UNIVERSAL_PROMPT_PREPARE_LOGIN = "duoUniversalPromptPrepareLoginAction";

    /**
     * Action id 'mfaVerifyTrustAction .
     */
    String ACTION_ID_MFA_VERIFY_TRUST_ACTION = "mfaVerifyTrustAction";

    /**
     * Action id 'mfaSetTrustAction .
     */
    String ACTION_ID_MFA_SET_TRUST_ACTION = "mfaSetTrustAction";

    /**
     * Action id 'mfaPrepareTrustDeviceViewAction .
     */
    String ACTION_ID_MFA_PREPARE_TRUST_DEVICE_VIEW_ACTION = "mfaPrepareTrustDeviceViewAction";

    /**
     * Action id 'accountMgmtRegistrationInitializeCaptchaAction .
     */
    String ACTION_ID_ACCOUNT_REGISTRATION_INIT_CAPTCHA = "accountMgmtRegistrationInitializeCaptchaAction";

    /**
     * Action id 'accountMgmtRegistrationValidateCaptchaAction .
     */
    String ACTION_ID_ACCOUNT_REGISTRATION_VALIDATE_CAPTCHA = "accountMgmtRegistrationValidateCaptchaAction";

    /**
     * Action id 'finalizeAccountRegistrationRequestAction .
     */
    String ACTION_ID_FINALIZE_ACCOUNT_REGISTRATION_REQUEST = "finalizeAccountRegistrationRequestAction";

    /**
     * Action id 'validateAccountRegistrationTokenAction .
     */
    String ACTION_ID_VALIDATE_ACCOUNT_REGISTRATION_TOKEN = "validateAccountRegistrationTokenAction";

    /**
     * Action id 'submitAccountRegistrationAction .
     */
    String ACTION_ID_ACCOUNT_REGISTRATION_SUBMIT = "submitAccountRegistrationAction";

    /**
     * Action id 'determineDuoUserAccountAction .
     */
    String ACTION_ID_DETERMINE_DUO_USER_ACCOUNT = "determineDuoUserAccountAction";

    /**
     * Action id 'checkWebAuthenticationRequestAction .
     */
    String ACTION_ID_CHECK_WEB_AUTHENTICATION_REQUEST = "checkWebAuthenticationRequestAction";

    /**
     * Action id 'prepareDuoWebLoginFormAction .
     */
    String ACTION_ID_PREPARE_DUO_WEB_LOGIN_FORM_ACTION = "prepareDuoWebLoginFormAction";

    /**
     * Action id 'duoAuthenticationWebflowAction .
     */
    String ACTION_ID_DUO_AUTHENTICATION_WEBFLOW = "duoAuthenticationWebflowAction";

    /**
     * Action id 'duoNonWebAuthenticationAction .
     */
    String ACTION_ID_DUO_NON_WEB_AUTHENTICATION_ACTION = "duoNonWebAuthenticationAction";

}
