package org.apereo.cas.adaptors.authy.core;

public class Constants {

    private Constants() {
    }

    public static final String[] ALLOWED_FACTORS = {"push", "token:software:totp", "sms", "email"};

    public static final String SELECT_MFA = "selectMFA";

    public static final String CHALLENGE = "challenge";
    public static final String CHALLENGE_PUSH = "challenge_push";

    public static final String ENROLL_SMS = "enroll_sms";
    public static final String ENROLL_EMAIL = "enroll_email";
    public static final String ENROLL_OKTA_PUSH = "enroll_okta_push";
    public static final String ENROLL_OKTA_TOTP = "enroll_okta_totp";
    public static final String ENROLL_GOOGLE = "enroll_google";

    public static final String STATE_ENROLL_PHONE = "phoneNumberEntryState";
    public static final String STATE_ENROLL_EMAIL = "emailAddressEntryState";
    public static final String STATE_ENROLL_OKTA = "oktaVerifyEntryState";
    public static final String STATE_ENROLL_GOOGLE = "googleEntryState";
    public static final String STATE_CHALLENGE= "verifyFactorState";
    public static final String STATE_CHALLENGE_PUSH = "pushValidationState";
    public static final String STATE_SELECT_MFA = "selectMfaState";

    public static final String ACTION_ENROLL_PHONE = "phoneEnrollmentAction";
    public static final String ACTION_ENROLL_EMAIL = "emailEnrollmentAction";
    public static final String ACTION_ENROLL_TOTP = "totpEnrollAction";
    public static final String ACTION_ENROLL_TOTP_VIEW = "totpEnrollViewAction";
    public static final String ACTION_USER_CHECK = "userCheckAction";
    public static final String ACTION_SELECT_MFA = "selectMfaAction";
    public static final String ACTION_CHALLENGE = "verifyFactorAction";
    public static final String ACTION_CHALLENGE_PUSH = "pushValidationAction";
    public static final String ACTION_RESEND_CODE = "resendCodeAction";

    public static final String VIEW_ENROLL_PHONE = "phoneNumberEntry";
    public static final String VIEW_ENROLL_EMAIL = "emailAddressEntry";
    public static final String VIEW_ENROLL_OKTA = "enrollOtp";
    public static final String VIEW_SELECT_MFA = "selectMfaView";
    public static final String VIEW_CHALLENGE = "verifyFactor";
    public static final String VIEW_CHALLENGE_PUSH = "pushValidation";

    public static final String TRANSITION_SELECT_MFA = "returnToMfaSelection";

    public static final String EVENT_SUCCESS_ID = "success";
    public static final String EVENT_ERROR_ID = "error";

    public static final String MESSAGE_CODE_ERROR = "Code invalide, veuillez réessayer";
    public static final String MESSAGE_TOTP_ERROR = "Vous n'avez pas enregistré votre appareil, veuillez réessayer";
}
