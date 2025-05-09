package org.apereo.cas.support.oauth;

/**
 * This class has the main constants for the OAuth implementation.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public interface OAuth20Constants {

    /**
     * Internal attribute to indicate whether the incoming request is an access token request.
     */
    String REQUEST_ATTRIBUTE_ACCESS_TOKEN_REQUEST = "oauth.request.access-token";

    /**
     * The prompt parameter.
     */
    String PROMPT = "prompt";

    /**
     * Device code flow verification uri.
     */
    String DEVICE_VERIFICATION_URI = "verification_uri";

    /**
     * Device code flow user code.
     */
    String DEVICE_USER_CODE = "user_code";

    /**
     * Device code flow device code.
     */
    String DEVICE_CODE = "device_code";

    /**
     * Device code flow refresh interval.
     */
    String DEVICE_INTERVAL = "interval";

    /**
     * Authentication context reference values.
     */
    String ACR_VALUES = "acr_values";

    /**
     * Base OAuth 2.0 url.
     */
    String BASE_OAUTH20_URL = "/oauth2.0";

    /**
     * The redirect uri.
     */
    String REDIRECT_URI = "redirect_uri";

    /**
     * The Authorization Server is unable to meet the requirements of the Relying Party for the authentication of the End-User.
     */
    String UNMET_AUTHENTICATION_REQUIREMENTS = "unmet_authentication_requirements";

    /**
     * The response type.
     */
    String RESPONSE_TYPE = "response_type";

    /**
     * The response mode.
     */
    String RESPONSE_MODE = "response_mode";

    /**
     * The claims.
     */
    String CLAIMS = "claims";

    /**
     * {@code userinfo} claims found as claims request parameter.
     */
    String CLAIMS_USERINFO = "userinfo";

    /**
     * The request passed as a jwt.
     */
    String REQUEST = "request";

    /**
     * The grant type.
     */
    String GRANT_TYPE = "grant_type";

    /**
     * The client id.
     */
    String CLIENT_ID = "client_id";

    /**
     * PKCE code challenge.
     */
    String CODE_CHALLENGE = "code_challenge";

    /**
     * PKCE code verifier.
     */
    String CODE_VERIFIER = "code_verifier";

    /**
     * PKCE code challenge method.
     */
    String CODE_CHALLENGE_METHOD = "code_challenge_method";

    /**
     * The client secret.
     */
    String CLIENT_SECRET = "client_secret";

    /**
     * The approval prompt.
     */
    String BYPASS_APPROVAL_PROMPT = "bypass_approval_prompt";

    /**
     * The scope request.
     */
    String SCOPE = "scope";

    /**
     * The code.
     */
    String CODE = "code";

    /**
     * The error.
     */
    String ERROR = "error";

    /**
     * The error description.
     */
    String ERROR_DESCRIPTION = "error_description";

    /**
     * The error with callback.
     */
    String ERROR_WITH_CALLBACK = "error_with_callback";

    /**
     * Unsupported response_type error.
     */
    String UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";

    /**
     * The state.
     */
    String STATE = "state";

    /**
     * The access token.
     */
    String TOKEN = "token";

    /**
     * The access token.
     */
    String ACCESS_TOKEN = "access_token";

    /**
     * The refresh token.
     */
    String REFRESH_TOKEN = "refresh_token";

    /**
     * The client assertion type.
     */
    String CLIENT_ASSERTION_TYPE = "client_assertion_type";

    /**
     * The client assertion jwt.
     */
    String CLIENT_ASSERTION = "client_assertion";

    /**
     * The missing access token.
     */
    String MISSING_ACCESS_TOKEN = "missing_accessToken";

    /**
     * The expired access token.
     */
    String EXPIRED_ACCESS_TOKEN = "expired_accessToken";

    /**
     * The confirm view.
     */
    String CONFIRM_VIEW = "oauthConfirmView";

    /**
     * The oauthSessionStaleMismatchErrorView view.
     */
    String SESSION_STALE_MISMATCH = "oauthSessionStaleMismatchErrorView";

    /**
     * The device code approval view.
     */
    String DEVICE_CODE_APPROVAL_VIEW = "oauthDeviceCodeApprovalView";

    /**
     * The device code approved view.
     */
    String DEVICE_CODE_APPROVED_VIEW = "oauthDeviceCodeApprovedView";

    /**
     * The invalid client.
     */
    String INVALID_CLIENT = "invalid_client";

    /**
     * The invalid request.
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * The invalid grant.
     */
    String INVALID_GRANT = "invalid_grant";

    /**
     * The invalid dpop.
     */
    String INVALID_DPOP_PROOF = "invalid_dpop_proof";

    /**
     * The invalid scope.
     */
    String INVALID_SCOPE = "invalid_scope";

    /**
     * Access denied error.
     */
    String ACCESS_DENIED = "access_denied";

    /**
     * Authz pending error.
     */
    String AUTHORIZATION_PENDING = "authorization_pending";

    /**
     * Authz state.
     */
    String AUTHORIZATION_STATE = "authorization_state";

    /**
     * Authz state claims submitted.
     */
    String CLAIMS_SUBMITTED = "claims_submitted";

    /**
     * slow down error for when approval requests are too quick.
     */
    String SLOW_DOWN = "slow_down";

    /**
     * The authorize url.
     */
    String AUTHORIZE_URL = "authorize";

    /**
     * The introspection url.
     */
    String INTROSPECTION_URL = "introspect";

    /**
     * Header value to signal JWT responses for token introspection payloads.
     */
    String INTROSPECTION_JWT_HEADER_CONTENT_TYPE = "application/token-introspection+jwt";

    /**
     * The callback authorize url.
     */
    String CALLBACK_AUTHORIZE_URL = "callbackAuthorize";

    /**
     * The callback authorize url definition.
     */
    String CALLBACK_AUTHORIZE_URL_DEFINITION = "callbackAuthorize.*";

    /**
     * The access token url.
     */
    String ACCESS_TOKEN_URL = "accessToken";

    /**
     * device authorization url.
     */
    String DEVICE_AUTHZ_URL = "device";

    /**
     * The token url.
     */
    String TOKEN_URL = "token";

    /**
     * The profile url.
     */
    String PROFILE_URL = "profile";

    /**
     * The revocation url.
     */
    String REVOCATION_URL = "revoke";

    /**
     * The remaining time in seconds before expiration.
     */
    String EXPIRES_IN = "expires_in";

    /**
     * The nonce parameter.
     */
    String NONCE = "nonce";

    /**
     * The token type parameter.
     */
    String TOKEN_TYPE = "token_type";

    /**
     * The bearer type.
     */
    String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * The DPOP Token.
     */
    String TOKEN_TYPE_DPOP = "DPoP";

    /**
     * Client assertion type as jwt bearer.
     */
    String CLIENT_ASSERTION_TYPE_JWT_BEARER = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    /**
     * Registration endpoint URL.
     */
    String UMA_REGISTRATION_URL = "register";

    /**
     * resource set registration endpoint URL.
     */
    String UMA_RESOURCE_SET_REGISTRATION_URL = "resourceSet";

    /**
     * permission registration endpoint URL.
     */
    String UMA_PERMISSION_URL = "permission";

    /**
     * policy registration endpoint URL.
     */
    String UMA_POLICY_URL = "policy";

    /**
     * authorization registration endpoint URL.
     */
    String UMA_AUTHORIZATION_REQUEST_URL = "rptAuthzRequest";

    /**
     * claims collection endpoint URL.
     */
    String UMA_CLAIMS_COLLECTION_URL = "rqpClaims";

    /**
     * URL endpoint that exposes JWKS for token signing.
     */
    String UMA_JWKS_URL = "umaJwks";

    /**
     * UMA protection scope.
     */
    String UMA_PROTECTION_SCOPE = "uma_protection";

    /**
     * UMA authz scope.
     */
    String UMA_AUTHORIZATION_SCOPE = "uma_authorization";

    /**
     * requesting party claim.
     */
    String REQUESTING_PARTY_CLAIMS = "requesting_party_claims";

    /**
     * error details.
     */
    String ERROR_DETAILS = "error_details";

    /**
     * need information.
     */
    String NEED_INFO = "need_info";

    /**
     * The unauthorized client.
     */
    String UNAUTHORIZED_CLIENT = "unauthorized_client";

    /**
     * DPoP header.
     */
    String DPOP = "DPoP";

    /**
     * resource parameter.
     */
    String RESOURCE = "resource";

    /**
     * audience parameter.
     */
    String AUDIENCE = "audience";

    /**
     * requested_token_type parameter.
     */
    String REQUESTED_TOKEN_TYPE = "requested_token_type";

    /**
     * subject_token parameter.
     */
    String SUBJECT_TOKEN = "subject_token";

    /**
     * subject_token_type parameter.
     */
    String SUBJECT_TOKEN_TYPE = "subject_token_type";
    /**
     * issued_token_type parameter.
     */
    String ISSUED_TOKEN_TYPE = "issued_token_type";
    /**
     * actor_token parameter.
     */
    String ACTOR_TOKEN = "actor_token";
    /**
     * act claim.
     */
    String CLAIM_ACT = "act";

    /**
     * The sub claim.
     */
    String CLAIM_SUB = "sub";
    /**
     * The exp claim.
     */
    String CLAIM_EXP = "exp";

    /**
     * actor_token_type parameter.
     */
    String ACTOR_TOKEN_TYPE = "actor_token_type";

    /**
     * DPoP confirmation that is put inside access token as an attribute.
     */
    String DPOP_CONFIRMATION = "DPoPConfirmation";

    /**
     * X509 certificate hash used and collected during TLS authentication.
     */
    String X509_CERTIFICATE_DIGEST = "x509_digest";

    /**
     * Stateless property.
     */
    String CAS_OAUTH_STATELESS_PROPERTY = "org.apereo.cas.oauth.property.stateless";
}
