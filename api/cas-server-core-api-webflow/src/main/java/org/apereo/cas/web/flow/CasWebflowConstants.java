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
     * Bean name for account profile flow registry.
     */
    String BEAN_NAME_ACCOUNT_PROFILE_FLOW_DEFINITION_REGISTRY = "accountProfileFlowRegistry";

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
     * The transition state 'passwordlessGetUserId'.
     */
    String TRANSITION_ID_PASSWORDLESS_GET_USERID = "passwordlessGetUserId";

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
     * The view id 'surrogateWildcardView'.
     */
    String TRANSITION_ID_SURROGATE_WILDCARD_VIEW = "surrogateWildcardView";

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
     * The transition state 'guaGetUserId'.
     */
    String TRANSITION_ID_GUA_GET_USERID = "guaGetUserId";

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
     * The transition state 'generate'.
     */
    String TRANSITION_ID_GENERATE = "generate";

    /**
     * Transition id 'delegatedAuthenticationRedirect' .
     */
    String TRANSITION_ID_DELEGATED_AUTHENTICATION_REDIRECT = "delegatedAuthenticationRedirect";

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
     * Transition id 'updateSecurityQuestions'.
     */
    String TRANSITION_ID_UPDATE_SECURITY_QUESTIONS = "updateSecurityQuestions";

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
     * Transition to determine the MFA failure mode and what action to take.
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
     * The state 'surrogateWildcardView'.
     */
    String STATE_ID_SURROGATE_WILDCARD_VIEW = "surrogateWildcardView";

    /**
     * The state 'loadSurrogatesAction'.
     */
    String STATE_ID_LOAD_SURROGATES_ACTION = "loadSurrogatesAction";

    /**
     * The state 'selectSurrogate'.
     */
    String STATE_ID_SELECT_SURROGATE = "selectSurrogate";

    /**
     * The state 'myAccountProfile'.
     */
    String STATE_ID_MY_ACCOUNT_PROFILE_VIEW = "myAccountProfile";

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
     * The state id 'wsFederationStart'.
     */
    String STATE_ID_WS_FEDERATION_START = "wsFederationStart";

    /**
     * The state id 'wsFederationRedirect'.
     */
    String STATE_ID_WS_FEDERATION_REDIRECT = "wsFederationRedirect";

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
    String STATE_ID_DELEGATED_AUTHENTICATION_REDIRECT_TO_AUTHN_PROVIDER = "redirectToDelegatedAuthnProviderView";

    /**
     * State id 'delegatedAuthenticationSelectCredential'.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION = "delegatedAuthenticationSelectCredential";
    /**
     * State id 'delegatedAuthenticationFinalizeCredential'.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_FINALIZE = "delegatedAuthenticationFinalizeCredential";


    /**
     * State id 'delegatedAuthenticationStoreWebflowState'.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_STORE = "delegatedAuthenticationStoreWebflowState";

    /**
     * State id 'delegatedAuthenticationClientSubflow'.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_SUBFLOW = "delegatedAuthenticationClientSubflow";


    /**
     * State id 'delegatedAuthenticationClientRedirect'.
     */
    String STATE_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT = "delegatedAuthenticationClientRedirect";

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
     * The state id 'finalizeInterruptFlow'.
     */
    String STATE_ID_INQUIRE_INTERRUPT = "inquireInterrupt";

    /**
     * The state id 'finalizeInterruptFlow'.
     */
    String STATE_ID_REDIRECT_INTERRUPT_LINK = "redirectToInterruptLink";

    /**
     * The state id 'finalizeInterruptFlow'.
     */
    String STATE_ID_FINALIZE_INTERRUPT = "finalizeInterruptFlow";

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
     * State id 'casAccountUnlockedView'.
     */
    String STATE_ID_ACCOUNT_UNLOCKED = "casAccountUnlockedView";

    /**
     * State id 'casBadHoursView'.
     */
    String STATE_ID_INVALID_AUTHENTICATION_HOURS = "casBadHoursView";
    /**
     * State id 'tokenAuthenticationCheck'.
     */
    String STATE_ID_TOKEN_AUTHENTICATION_CHECK = "tokenAuthenticationCheck";

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
     * State id 'passwordChangeRequest'.
     */
    String STATE_ID_PASSWORD_CHANGE_REQUEST = "passwordChangeRequest";

    /**
     * State id 'updateSecurityQuestions'.
     */
    String STATE_ID_UPDATE_SECURITY_QUESTIONS = "updateSecurityQuestions";

    /**
     * State id 'viewRegistration'.
     */
    String STATE_ID_VIEW_REGISTRATION = "viewRegistration";

    /**
     * State id 'viewRegistrationWebAuthn'.
     */
    String STATE_ID_WEBAUTHN_VIEW_REGISTRATION = "viewRegistrationWebAuthn";

    /**
     * State id 'validateWebAuthnToken'.
     */
    String STATE_ID_WEBAUTHN_VALIDATE = "validateWebAuthnToken";

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
     * State id 'acceptUserGraphicsForAuthentication'.
     */
    String STATE_ID_ACCEPT_GUA = "acceptUserGraphicsForAuthentication";
    /**
     * State id 'guaGetUserIdView'.
     */
    String STATE_ID_GUA_GET_USERID = "guaGetUserIdView";
    /**
     * State id 'guaDisplayUserGraphics'.
     */
    String STATE_ID_GUA_DISPLAY_USER_GFX = "guaDisplayUserGraphics";

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

    /**
     * State id 'passwordlessDisplayUser'.
     */
    String STATE_ID_PASSWORDLESS_DISPLAY = "passwordlessDisplayUser";

    /**
     * State id 'determineMultifactorPasswordlessAuthentication'.
     */
    String STATE_ID_PASSWORDLESS_DETERMINE_MFA = "determineMultifactorPasswordlessAuthentication";

    /**
     * State id 'determineDelegatedAuthentication'.
     */
    String STATE_ID_PASSWORDLESS_DETERMINE_DELEGATED_AUTHN = "determineDelegatedAuthentication";

    /**
     * State id 'passwordlessVerifyAccount'.
     */
    String STATE_ID_PASSWORDLESS_VERIFY_ACCOUNT = "passwordlessVerifyAccount";

    /**
     * State id 'acceptPasswordlessAuthentication'.
     */
    String STATE_ID_ACCEPT_PASSWORDLESS_AUTHENTICATION = "acceptPasswordlessAuthentication";

    /**
     * State id 'passwordlessGetUserIdView'.
     */
    String STATE_ID_PASSWORDLESS_GET_USERID = "passwordlessGetUserIdView";

    /**
     * State id 'sendSimpleToken'.
     */
    String STATE_ID_SIMPLE_MFA_SEND_TOKEN = "sendSimpleToken";

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
     * The view state 'casDelegatedAuthnStopWebflow'.
     */
    String VIEW_ID_DELEGATED_AUTHENTICATION_STOP_WEBFLOW = "delegated-authn/casDelegatedAuthnStopWebflow";

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
     * Action to check if delegated authentication has failed.
     */
    String DECISION_STATE_CHECK_DELEGATED_AUTHN_FAILURE = "checkDelegatedAuthnFailureDecision";

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
     * Action id 'accountUnlockStatusPrepareAction'.
     */
    String ACTION_ID_ACCOUNT_UNLOCK_PREPARE = "accountUnlockStatusPrepareAction";


    /**
     * Action id 'delegatedAuthenticationClientCredentialSelectionFinalizeAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION_FINALIZE = "delegatedAuthenticationClientCredentialSelectionFinalizeAction";

    /**
     * Action id 'delegatedAuthenticationClientCredentialSelectionAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_CREDENTIAL_SELECTION = "delegatedAuthenticationClientCredentialSelectionAction";

    /**
     * Action id 'delegatedAuthenticationProviderDynamicDiscoveryAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_DYNAMIC_DISCOVERY_EXECUTION = "delegatedAuthenticationProviderDynamicDiscoveryExecutionAction";

    /**
     * Action id 'delegatedAuthenticationRedirectToClientAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CLIENT_REDIRECT = "delegatedAuthenticationRedirectToClientAction";

    /**
     * Action id 'delegatedAuthenticationStoreWebflowAction'.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_STORE_WEBFLOW_STATE = "delegatedAuthenticationStoreWebflowAction";

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
     * Action id 'accountUnlockStatusAction .
     */
    String ACTION_ID_UNLOCK_ACCOUNT_STATUS = "accountUnlockStatusAction";

    /**
     * Action id 'initPasswordChangeAction .
     */
    String ACTION_ID_INIT_PASSWORD_CHANGE = "initPasswordChangeAction";

    /**
     * Action id 'renderLoginFormAction'.
     */
    String ACTION_ID_RENDER_LOGIN_FORM = "renderLoginFormAction";

    /**
     * Action id 'setServiceUnauthorizedRedirectUrlAction'.
     */
    String ACTION_ID_SET_SERVICE_UNAUTHORIZED_REDIRECT_URL = "setServiceUnauthorizedRedirectUrlAction";

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
     * Action id 'fetchTicketGrantingTicketAction'.
     */
    String ACTION_ID_FETCH_TICKET_GRANTING_TICKET = "fetchTicketGrantingTicketAction";

    /**
     * Action id 'ticketGrantingTicketCheckAction'.
     */
    String ACTION_ID_TICKET_GRANTING_TICKET_CHECK = "ticketGrantingTicketCheckAction";
    /**
     * Action id 'frontChannelLogoutAction'.
     */
    String ACTION_ID_FRONT_CHANNEL_LOGOUT = "frontChannelLogoutAction";

    /**
     * Action id 'gatewayServicesManagementCheck'.
     */
    String ACTION_ID_GATEWAY_SERVICES_MANAGEMENT = "gatewayServicesManagementCheck";

    /**
     * Action id 'prepareAccountProfileViewAction'.
     */
    String ACTION_ID_PREPARE_ACCOUNT_PROFILE = "prepareAccountProfileViewAction";

    /**
     * Action id 'accountProfilePasswordChangeRequestAction'.
     */
    String ACTION_ID_ACCOUNT_PROFILE_PASSWORD_CHANGE_REQUEST = "accountProfilePasswordChangeRequestAction";


    /**
     * Action id 'prepareAccountProfilePasswordMgmtAction'.
     */
    String ACTION_ID_PREPARE_ACCOUNT_PASSWORD_MANAGEMENT = "prepareAccountProfilePasswordMgmtAction";

    /**
     * Action id 'initialAuthenticationRequestValidationAction'.
     */
    String ACTION_ID_INITIAL_AUTHN_REQUEST_VALIDATION = "initialAuthenticationRequestValidationAction";

    /**
     * Action id 'genericSuccessViewAction'.
     */
    String ACTION_ID_GENERIC_SUCCESS_VIEW = "genericSuccessViewAction";

    /**
     * Action id 'remoteAuthenticate'.
     */
    String ACTION_ID_REMOTE_TRUSTED_AUTHENTICATION = "remoteAuthenticate";

    /**
     * Action id 'clearWebflowCredentialsAction'.
     */
    String ACTION_ID_CLEAR_WEBFLOW_CREDENTIALS = "clearWebflowCredentialsAction";

    /**
     * Action id 'redirectUnauthorizedServiceUrlAction'.
     */
    String ACTION_ID_REDIRECT_UNAUTHORIZED_SERVICE_URL = "redirectUnauthorizedServiceUrlAction";

    /**
     * Action id 'generateServiceTicketAction'.
     */
    String ACTION_ID_GENERATE_SERVICE_TICKET = "generateServiceTicketAction";

    /**
     * Action id 'redirectToServiceAction'.
     */
    String ACTION_ID_REDIRECT_TO_SERVICE = "redirectToServiceAction";

    /**
     * Action id 'terminateSessionAction'.
     */
    String ACTION_ID_TERMINATE_SESSION = "terminateSessionAction";

    /**
     * Action id 'confirmLogoutAction'.
     */
    String ACTION_ID_CONFIRM_LOGOUT = "confirmLogoutAction";

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
     * Action id 'singleSignOnSessionCreated'.
     */
    String ACTION_ID_SINGLE_SIGON_SESSION_CREATED = "singleSignOnSessionCreated";

    /**
     * Action id 'createTicketGrantingTicketAction'.
     */
    String ACTION_ID_CREATE_TICKET_GRANTING_TICKET = "createTicketGrantingTicketAction";

    /**
     * Action id `delegatedAuthenticationAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION = "delegatedAuthenticationAction";

    /**
     * Action id `delegatedAuthenticationCreateClientsAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_CREATE_CLIENTS = "delegatedAuthenticationCreateClientsAction";

    /**
     * Action id `delegatedAuthenticationFailureAction`.
     */
    String ACTION_ID_DELEGATED_AUTHENTICATION_FAILURE = "delegatedAuthenticationFailureAction";

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
     * Action id 'negociateSpneg .
     */
    String ACTION_ID_SPNEGO_NEGOTIATE = "negociateSpnego";

    /**
     * Action id 'acceptableUsagePolicyVerifyAction.
     */
    String ACTION_ID_AUP_VERIFY = "acceptableUsagePolicyVerifyAction";

    /**
     * Action id 'acceptableUsagePolicySubmitAction.
     */
    String ACTION_ID_AUP_SUBMIT = "acceptableUsagePolicySubmitAction";

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
     * Action id 'surrogateInitialAuthenticationAction'.
     */
    String ACTION_ID_SURROGATE_INITIAL_AUTHENTICATION = "surrogateInitialAuthenticationAction";

    /**
     * Action id 'loadSurrogatesListAction .
     */
    String ACTION_ID_LOAD_SURROGATES_LIST_ACTION = "loadSurrogatesListAction";

    /**
     * Action id 'selectSurrogateAction .
     */
    String ACTION_ID_SELECT_SURROGATE_ACTION = "selectSurrogateAction";
    /**
     * Action id 'duoUniversalPromptValidateLoginAction .
     */
    String ACTION_ID_DUO_UNIVERSAL_PROMPT_VALIDATE_LOGIN = "duoUniversalPromptValidateLoginAction";

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
    String ACTION_ID_PREPARE_DUO_WEB_LOGIN_FORM = "prepareDuoWebLoginFormAction";

    /**
     * Action id 'duoAuthenticationWebflowAction .
     */
    String ACTION_ID_DUO_AUTHENTICATION_WEBFLOW = "duoAuthenticationWebflowAction";

    /**
     * Action id 'duoNonWebAuthenticationAction .
     */
    String ACTION_ID_DUO_NON_WEB_AUTHENTICATION = "duoNonWebAuthenticationAction";

    /**
     * Action id 'displayBeforePasswordlessAuthenticationAction .
     */
    String ACTION_ID_DISPLAY_BEFORE_PASSWORDLESS_AUTHN = "displayBeforePasswordlessAuthenticationAction";

    /**
     * Action id 'passswordPrepareLoginAction .
     */
    String ACTION_ID_PASSWORDLESS_PREPARE_LOGIN = "passswordPrepareLoginAction";

    /**
     * Action id 'verifyPasswordlessAccountAuthenticationAction .
     */
    String ACTION_ID_VERIFY_PASSWORDLESS_ACCOUNT_AUTHN = "verifyPasswordlessAccountAuthenticationAction";

    /**
     * Action id 'verifyPasswordlessAccountAuthenticationAction .
     */
    String ACTION_ID_DETERMINE_PASSWORDLESS_DELEGATED_AUTHN = "determineDelegatedAuthenticationAction";

    /**
     * Action id 'determineMultifactorPasswordlessAuthenticationAction .
     */
    String ACTION_ID_DETERMINE_PASSWORDLESS_MULTIFACTOR_AUTHN = "determineMultifactorPasswordlessAuthenticationAction";

    /**
     * Action id 'determineMultifactorPasswordlessAuthenticationAction .
     */
    String ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN = "acceptPasswordlessAuthenticationAction";

    /**
     * Action id 'webAuthnStartAuthenticationAction .
     */
    String ACTION_ID_WEBAUTHN_START_AUTHENTICATION = "webAuthnStartAuthenticationAction";

    /**
     * Action id 'webAuthnAuthenticationWebflowAction .
     */
    String ACTION_ID_WEBAUTHN_AUTHENTICATION_WEBFLOW = "webAuthnAuthenticationWebflowAction";

    /**
     * Action id 'webAuthnSaveAccountRegistrationAction .
     */
    String ACTION_ID_WEBAUTHN_SAVE_ACCOUNT_REGISTRATION = "webAuthnSaveAccountRegistrationAction";

    /**
     * Action id 'webAuthnCheckAccountRegistrationAction .
     */
    String ACTION_ID_WEBAUTHN_CHECK_ACCOUNT_REGISTRATION = "webAuthnCheckAccountRegistrationAction";

    /**
     * Action id 'webAuthnStartRegistrationAction .
     */
    String ACTION_ID_WEB_AUTHN_START_REGISTRATION = "webAuthnStartRegistrationAction";
    /**
     * Action id 'tokenAuthenticationAction .
     */
    String ACTION_ID_TOKEN_AUTHENTICATION_ACTION = "tokenAuthenticationAction";

    /**
     * Action id 'webAuthnValidateSessionCredentialTokenAction .
     */
    String ACTION_ID_WEBAUTHN_VALIDATE_SESSION_CREDENTIAL_TOKEN = "webAuthnValidateSessionCredentialTokenAction";

    /**
     * Action id 'wsFederationAction'.
     */
    String ACTION_ID_WS_FEDERATION = "wsFederationAction";

    /**
     * Action id 'wsFederationClientRedirectAction'.
     */
    String ACTION_ID_WS_FEDERATION_REDIRECT = "wsFederationClientRedirectAction";

    /**
     * Action id 'mfaAvailableAction'.
     */
    String ACTION_ID_MFA_CHECK_AVAILABLE = "mfaAvailableAction";

    /**
     * Action id 'mfaBypassAction'.
     */
    String ACTION_ID_MFA_CHECK_BYPASS = "mfaBypassAction";

    /**
     * Action id 'mfaFailureAction'.
     */
    String ACTION_ID_MFA_CHECK_FAILURE = "mfaFailureAction";

    /**
     * Action id 'consentAccountProfilePrepareAction'.
     */
    String ACTION_ID_CONSENT_ACCOUNT_PROFILE_PREPARE = "consentAccountProfilePrepareAction";

    /**
     * Action id 'checkConsentRequiredAction'.
     */
    String ACTION_ID_CHECK_CONSENT_REQUIRED = "checkConsentRequiredAction";
    /**
     * Action id 'confirmConsentAction'.
     */
    String ACTION_ID_CONFIRM_CONSENT = "confirmConsentAction";
    /**
     * Action id 'mfaSimpleMultifactorSendTokenAction'.
     */
    String ACTION_ID_MFA_SIMPLE_SEND_TOKEN = "mfaSimpleMultifactorSendTokenAction";
    /**
     * Action id 'authyAuthenticationWebflowAction'.
     */
    String ACTION_ID_AUTHY_AUTHENTICATION = "authyAuthenticationWebflowAction";

    /**
     * Action id 'authyAuthenticationRegistrationWebflowAction'.
     */
    String ACTION_ID_AUTHY_REGISTRATION = "authyAuthenticationRegistrationWebflowAction";

    /**
     * Action id 'loadAccountRegistrationPropertiesAction'.
     */
    String ACTION_ID_LOAD_ACCOUNT_REGISTRATION_PROPERTIES = "loadAccountRegistrationPropertiesAction";

    /**
     * Action id 'googleSaveAccountRegistrationAction'.
     */
    String ACTION_ID_GOOGLE_SAVE_ACCOUNT_REGISTRATION = "googleSaveAccountRegistrationAction";

    /**
     * Action id 'googleAccountCheckRegistrationAction'.
     */
    String ACTION_ID_GOOGLE_CHECK_ACCOUNT_REGISTRATION = "googleAccountCheckRegistrationAction";

    /**
     * Action id 'googleAccountConfirmSelectionAction'.
     */
    String ACTION_ID_GOOGLE_CONFIRM_SELECTION = "googleAccountConfirmSelectionAction";
    /**
     * Action id 'googleAccountDeleteDeviceAction'.
     */
    String ACTION_ID_GOOGLE_ACCOUNT_DELETE_DEVICE = "googleAccountDeleteDeviceAction";
    /**
     * Action id 'prepareGoogleAuthenticatorLoginAction'.
     */
    String ACTION_ID_GOOGLE_PREPARE_LOGIN = "prepareGoogleAuthenticatorLoginAction";
    /**
     * Action id 'googleAccountCreateRegistrationAction'.
     */
    String ACTION_ID_GOOGLE_ACCOUNT_CREATE_REGISTRATION = "googleAccountCreateRegistrationAction";

    /**
     * Action id 'validateSelectedRegistrationAction'.
     */
    String ACTION_ID_GOOGLE_VALIDATE_SELECTED_REGISTRATION = "validateSelectedRegistrationAction";

    /**
     * Action id 'yubikeyAuthenticationWebflowAction'.
     */
    String ACTION_ID_YUBIKEY_AUTHENTICATION = "yubikeyAuthenticationWebflowAction";

    /**
     * Action id 'prepareYubiKeyAuthenticationLoginAction'.
     */
    String ACTION_ID_YUBIKEY_PREPARE_LOGIN = "prepareYubiKeyAuthenticationLoginAction";

    /**
     * Action id 'yubiKeyAccountRegistrationAction'.
     */
    String ACTION_ID_YUBIKEY_ACCOUNT_REGISTRATION = "yubiKeyAccountRegistrationAction";

    /**
     * Action id 'yubiKeySaveAccountRegistrationAction'.
     */
    String ACTION_ID_YUBIKEY_SAVE_ACCOUNT_REGISTRATION = "yubiKeySaveAccountRegistrationAction";

    /**
     * Action id 'initPasswordResetAction'.
     */
    String ACTION_ID_PASSWORD_RESET_INIT = "initPasswordResetAction";

    /**
     * Action id 'passwordChangeAction'.
     */
    String ACTION_ID_PASSWORD_CHANGE = "passwordChangeAction";

    /**
     * Action id 'sendPasswordResetInstructionsAction'.
     */
    String ACTION_ID_PASSWORD_RESET_SEND_INSTRUCTIONS = "sendPasswordResetInstructionsAction";

    /**
     * Action id 'verifyPasswordResetRequestAction'.
     */
    String ACTION_ID_PASSWORD_RESET_VERIFY_REQUEST = "verifyPasswordResetRequestAction";

    /**
     * Action id 'verifySecurityQuestionsAction'.
     */
    String ACTION_ID_PASSWORD_RESET_VERIFY_SECURITY_QUESTIONS = "verifySecurityQuestionsAction";

    /**
     * Action id 'validatePasswordResetTokenAction'.
     */
    String ACTION_ID_PASSWORD_RESET_VALIDATE_TOKEN = "validatePasswordResetTokenAction";

    /**
     * Action id 'u2fAuthenticationWebflowAction'.
     */
    String ACTION_ID_U2F_AUTHENTICATION = "u2fAuthenticationWebflowAction";

    /**
     * Action id 'u2fStartAuthenticationAction'.
     */
    String ACTION_ID_U2F_START_AUTHENTICATION = "u2fStartAuthenticationAction";

    /**
     * Action id 'u2fStartRegistrationAction'.
     */
    String ACTION_ID_U2F_START_REGISTRATION = "u2fStartRegistrationAction";

    /**
     * Action id 'u2fCheckAccountRegistrationAction'.
     */
    String ACTION_ID_U2F_CHECK_REGISTRATION = "u2fCheckAccountRegistrationAction";

    /**
     * Action id 'u2fSaveAccountRegistrationAction'.
     */
    String ACTION_ID_U2F_SAVE_REGISTRATION = "u2fSaveAccountRegistrationAction";

    /**
     * Action id 'remoteAddressCheck'.
     */
    String ACTION_ID_REMOTE_AUTHENTICATION_ADDRESS_CHECK = "remoteAddressCheck";

    /**
     * Action id 'basicAuthenticationAction'.
     */
    String ACTION_ID_BASIC_AUTHENTICATION = "basicAuthenticationAction";

    /**
     * Action id 'digestAuthenticationAction'.
     */
    String ACTION_ID_DIGEST_AUTHENTICATION = "digestAuthenticationAction";

    /**
     * Action id 'principalScimProvisionerAction'.
     */
    String ACTION_ID_SCIM_PROVISIONING_PRINCIPAL = "principalScimProvisionerAction";

    /**
     * Action id 'radiusAuthenticationWebflowAction'.
     */
    String ACTION_ID_RADIUS_AUTHENTICATION = "radiusAuthenticationWebflowAction";

    /**
     * Action id 'surrogateAuthorizationCheck'.
     */
    String ACTION_ID_SURROGATE_AUTHORIZATION_CHECK = "surrogateAuthorizationCheck";

    /**
     * Action id 'handlePasswordExpirationWarningMessagesAction'.
     */
    String ACTION_ID_PASSWORD_EXPIRATION_HANDLE_WARNINGS = "handlePasswordExpirationWarningMessagesAction";

    /**
     * Action id 'removeGoogleAnalyticsCookieAction'.
     */
    String ACTION_ID_GOOGLE_ANALYTICS_REMOVE_COOKIE = "removeGoogleAnalyticsCookieAction";

    /**
     * Action id 'createGoogleAnalyticsCookieAction'.
     */
    String ACTION_ID_GOOGLE_ANALYTICS_CREATE_COOKIE = "createGoogleAnalyticsCookieAction";

    /**
     * Action id 'qrAuthenticationValidateWebSocketChannelAction'.
     */
    String ACTION_ID_QR_AUTHENTICATION_VALIDATE_CHANNEL = "qrAuthenticationValidateWebSocketChannelAction";

    /**
     * Action id 'qrAuthenticationGenerateCodeAction'.
     */
    String ACTION_ID_QR_AUTHENTICATION_GENERATE_CODE = "qrAuthenticationGenerateCodeAction";

    /**
     * Action id 'acceptUserGraphicsForAuthenticationAction'.
     */
    String ACTION_ID_GUA_ACCEPT_USER = "acceptUserGraphicsForAuthenticationAction";
    /**
     * Action id 'acceptUserGraphicsForAuthenticationAction'.
     */
    String ACTION_ID_GUA_DISPLAY_USER_GRAPHICS_BEFORE_AUTHENTICATION = "displayUserGraphicsBeforeAuthenticationAction";

    /**
     * Action id 'prepareForGraphicalAuthenticationAction'.
     */
    String ACTION_ID_GUA_PREPARE_LOGIN = "prepareForGraphicalAuthenticationAction";

    /**
     * Action id 'samlMetadataUIParserAction'.
     */
    String ACTION_ID_SAML_METADATA_UI_PARSER = "samlMetadataUIParserAction";

    /**
     * Action id 'samlIdPSessionStoreTicketGrantingTicketAction'.
     */
    String ACTION_ID_SAML_IDP_SESSION_STORE_TICKET_GRANTING_TICKET = "samlIdPSessionStoreTicketGrantingTicketAction";

    /**
     * Action id 'samlIdPMetadataUIParserAction'.
     */
    String ACTION_ID_SAML_IDP_METADATA_UI_PARSER = "samlIdPMetadataUIParserAction";

    /**
     * Action id 'oidcRegisteredServiceUIAction'.
     */
    String ACTION_ID_OIDC_REGSTERED_SERVICE_UI = "oidcRegisteredServiceUIAction";

    /**
     * Action id 'oauth20RegisteredServiceUIAction'.
     */
    String ACTION_ID_OAUTH20_REGISTERED_SERVICE_UI = "oauth20RegisteredServiceUIAction";

    /**
     * Action id 'oauth20SessionStoreTicketGrantingTicketAction'.
     */
    String ACTION_ID_OAUTH20_SESSION_STORE_TICKET_GRANTING_TICKET = "oauth20SessionStoreTicketGrantingTicketAction";

    /**
     * Action id 'inweboPushAuthenticateAction'.
     */
    String ACTION_ID_INWEBO_PUSH_AUTHENTICATION = "inweboPushAuthenticateAction";

    /**
     * Action id 'inweboCheckUserAction'.
     */
    String ACTION_ID_INWEBO_CHECK_USER = "inweboCheckUserAction";

    /**
     * Action id 'inweboMustEnrollAction'.
     */
    String ACTION_ID_INWEBO_MUST_ENROLL = "inweboMustEnrollAction";

    /**
     * Action id 'inweboCheckAuthenticationAction'.
     */
    String ACTION_ID_INWEBO_CHECK_AUTHENTICATION = "inweboCheckAuthenticationAction";

    /**
     * Action id 'inweboSuccessAction'.
     */
    String ACTION_ID_INWEBO_SUCCESS = "inweboSuccessAction";

    /**
     * Action id 'mfaAccepttoMultifactorValidateUserDeviceRegistrationAction'.
     */
    String ACTION_ID_ACCEPTTO_VALIDATE_USER_DEVICE_REGISTRATION = "mfaAccepttoMultifactorValidateUserDeviceRegistrationAction";

    /**
     * Action id 'mfaAccepttoMultifactorFetchChannelAction'.
     */
    String ACTION_ID_ACCEPTTO_FETCH_CHANNEL = "mfaAccepttoMultifactorFetchChannelAction";

    /**
     * Action id 'mfaAccepttoMultifactorValidateChannelAction'.
     */
    String ACTION_ID_ACCEPTTO_VALIDATE_CHANNEL = "mfaAccepttoMultifactorValidateChannelAction";

    /**
     * Action id 'mfaAccepttoQRCodeValidateWebSocketChannelAction'.
     */
    String ACTION_ID_ACCEPTTO_QR_CODE_VALIDATE_CHANNEL = "mfaAccepttoQRCodeValidateWebSocketChannelAction";

    /**
     * Action id 'mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction'.
     */
    String ACTION_ID_ACCEPTTO_FINALIZE_AUTHENTICATION = "mfaAccepttoMultifactorFinalizeAuthenticationWebflowAction";

    /**
     * Action id 'mfaAccepttoMultifactorDetermineUserAccountStatusAction'.
     */
    String ACTION_ID_ACCEPTTO_DETERMINE_USER_ACCOUNT_STATUS = "mfaAccepttoMultifactorDetermineUserAccountStatusAction";

    /**
     * Action id 'wsFederationMetadataUIAction'.
     */
    String ACTION_ID_WSFEDERATION_METADATA_UI = "wsFederationMetadataUIAction";

    /**
     * Action id 'accountProfileUpdateSecurityQuestionsAction'.
     */
    String ACTION_ID_ACCOUNT_PROFILE_UPDATE_SECURITY_QUESTIONS = "accountProfileUpdateSecurityQuestionsAction";

    /**
     * Action id 'ldapSpnegoClientAction'.
     */
    String ACTION_ID_SPNEGO_CLIENT_LDAP = "ldapSpnegoClientAction";

    /**
     * Action id 'hostnameSpnegoClientAction'.
     */
    String ACTION_ID_SPNEGO_CLIENT_HOSTNAME = "hostnameSpnegoClientAction";

    /**
     * Action id 'baseSpnegoClientAction'.
     */
    String ACTION_ID_SPNEGO_CLIENT_BASE = "baseSpnegoClientAction";

    /**
     * The action id 'spnego'.
     */
    String ACTION_ID_SPNEGO = "spnego";
}
