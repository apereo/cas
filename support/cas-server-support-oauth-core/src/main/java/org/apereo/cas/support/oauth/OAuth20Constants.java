package org.apereo.cas.support.oauth;

/**
 * This class has the main constants for the OAuth implementation.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public interface OAuth20Constants {

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
     * The response type.
     */
    String RESPONSE_TYPE = "response_type";

    /**
     * The grant type.
     */
    String GRANT_TYPE = "grant_type";

    /**
     * The client id.
     */
    String CLIENT_ID = "client_id";

    /**
     * The client secret.
     */
    String SECRET = "secret";

    /**
     * The client username.
     */
    String USERNAME = "username";

    /**
     * The client password.
     */
    String PASSWORD = "password";

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
     * The bearer token.
     */
    String BEARER_TOKEN = "Bearer";

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
     * The device code approval view.
     */
    String DEVICE_CODE_APPROVAL_VIEW = "oauthDeviceCodeApprovalView";

    /**
     * The device code approved view.
     */
    String DEVICE_CODE_APPROVED_VIEW = "oauthDeviceCodeApprovedView";

    /**
     * The error view.
     */
    String ERROR_VIEW = "casServiceErrorView";

    /**
     * The invalid request.
     */
    String INVALID_REQUEST = "invalid_request";

    /**
     * The invalid grant.
     */
    String INVALID_GRANT = "invalid_grant";

    /**
     * Access denied error.
     */
    String ACCESS_DENIED = "access_denied";

    /**
     * Authz pending error.
     */
    String AUTHORIZATION_PENDING = "authorization_pending";

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
     * The remaining time in seconds before expiration with syntax : expires=3600...
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
    String TOKEN_TYPE_BEARER = "bearer";
}
