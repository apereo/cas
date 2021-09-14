package org.apereo.cas.audit;

/**
 * This is {@link AuditableActions}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface AuditableActions {
    /**
     * Auditable action {@code MULTIFACTOR_AUTHENTICATION_BYPASS}.
     */
    String MULTIFACTOR_AUTHENTICATION_BYPASS = "MULTIFACTOR_AUTHENTICATION_BYPASS";
    /**
     * Auditable action {@code AUTHENTICATION_EVENT}.
     */
    String AUTHENTICATION_EVENT = "AUTHENTICATION_EVENT";
    /**
     * Auditable action {@code AUTHENTICATION}.
     */
    String AUTHENTICATION = "AUTHENTICATION";
    /**
     * Auditable action {@code MULTIFACTOR_AUTHENTICATION_BYPASS}.
     */
    String SERVICE_TICKET = "SERVICE_TICKET";
    /**
     * Auditable action {@code MULTIFACTOR_AUTHENTICATION_BYPASS}.
     */
    String PROXY_TICKET = "PROXY_TICKET";
    /**
     * Auditable action {@code TICKET_GRANTING_TICKET}.
     */
    String TICKET_GRANTING_TICKET = "TICKET_GRANTING_TICKET";
    /**
     * Auditable action {@code TICKET_DESTROYED}.
     */
    String TICKET_DESTROYED = "TICKET_DESTROYED";
    /**
     * Auditable action {@code PROXY_GRANTING_TICKET}.
     */
    String PROXY_GRANTING_TICKET = "PROXY_GRANTING_TICKET";
    /**
     * Auditable action {@code SERVICE_TICKET_VALIDATE}.
     */
    String SERVICE_TICKET_VALIDATE = "SERVICE_TICKET_VALIDATE";
    /**
     * Auditable action {@code PROTOCOL_SPECIFICATION_VALIDATE}.
     */
    String PROTOCOL_SPECIFICATION_VALIDATE = "PROTOCOL_SPECIFICATION_VALIDATE";

    /**
     * Auditable action {@code REST_API_SERVICE_TICKET}.
     */
    String REST_API_SERVICE_TICKET = "REST_API_SERVICE_TICKET";
    /**
     * Auditable action {@code REST_API_TICKET_GRANTING_TICKET}.
     */
    String REST_API_TICKET_GRANTING_TICKET = "REST_API_TICKET_GRANTING_TICKET";

    /**
     * Auditable action {@code EVALUATE_RISKY_AUTHENTICATION}.
     */
    String EVALUATE_RISKY_AUTHENTICATION = "EVALUATE_RISKY_AUTHENTICATION";
    /**
     * Auditable action {@code MITIGATE_RISKY_AUTHENTICATION}.
     */
    String MITIGATE_RISKY_AUTHENTICATION = "MITIGATE_RISKY_AUTHENTICATION";

    /**
     * Auditable action {@code TRUSTED_AUTHENTICATION}.
     */
    String TRUSTED_AUTHENTICATION = "TRUSTED_AUTHENTICATION";

    /**
     * Auditable action {@code SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION}.
     */
    String SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION = "SURROGATE_AUTHENTICATION_ELIGIBILITY_SELECTION";
    /**
     * Auditable action {@code SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION}.
     */
    String SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION = "SURROGATE_AUTHENTICATION_ELIGIBILITY_VERIFICATION";

    /**
     * Auditable action {@code SERVICE_ACCESS_ENFORCEMENT}.
     */
    String SERVICE_ACCESS_ENFORCEMENT = "SERVICE_ACCESS_ENFORCEMENT";
    /**
     * Auditable action {@code SAVE_SERVICE}.
     */
    String SAVE_SERVICE = "SAVE_SERVICE";
    /**
     * Auditable action {@code DELETE_SERVICE}.
     */
    String DELETE_SERVICE = "DELETE_SERVICE";

    /**
     * Auditable action {@code DELEGATED_CLIENT}.
     */
    String DELEGATED_CLIENT = "DELEGATED_CLIENT";
    /**
     * Auditable action {@code SAVE_CONSENT}.
     */
    String SAVE_CONSENT = "SAVE_CONSENT";

    /**
     * Auditable action {@code SAML2_RESPONSE}.
     */
    String SAML2_RESPONSE = "SAML2_RESPONSE";
    /**
     * Auditable action {@code SAML2_REQUEST}.
     */
    String SAML2_REQUEST = "SAML2_REQUEST";

    /**
     * Auditable action {@code OAUTH2_ACCESS_TOKEN_RESPONSE}.
     */
    String OAUTH2_ACCESS_TOKEN_RESPONSE = "OAUTH2_ACCESS_TOKEN_RESPONSE";
    /**
     * Auditable action {@code OAUTH2_USER_PROFILE}.
     */
    String OAUTH2_USER_PROFILE = "OAUTH2_USER_PROFILE";
    /**
     * Auditable action {@code OAUTH2_CODE_RESPONSE}.
     */
    String OAUTH2_CODE_RESPONSE = "OAUTH2_CODE_RESPONSE";
    /**
     * Auditable action {@code OAUTH2_ACCESS_TOKEN_REQUEST}.
     */
    String OAUTH2_ACCESS_TOKEN_REQUEST = "OAUTH2_ACCESS_TOKEN_REQUEST";

    /**
     * Auditable action {@code AUP_VERIFY}.
     */
    String AUP_VERIFY = "AUP_VERIFY";
    /**
     * Auditable action {@code AUP_SUBMIT}.
     */
    String AUP_SUBMIT = "AUP_SUBMIT";

    /**
     * Auditable action {@code CHANGE_PASSWORD}.
     */
    String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    /**
     * Auditable action {@code REQUEST_CHANGE_PASSWORD}.
     */
    String REQUEST_CHANGE_PASSWORD = "REQUEST_CHANGE_PASSWORD";
    /**
     * Auditable action {@code REQUEST_FORGOT_USERNAME}.
     */
    String REQUEST_FORGOT_USERNAME = "REQUEST_FORGOT_USERNAME";

    /**
     * Auditable action {@code ACCOUNT_REGISTRATION}.
     */
    String ACCOUNT_REGISTRATION = "ACCOUNT_REGISTRATION";
}
