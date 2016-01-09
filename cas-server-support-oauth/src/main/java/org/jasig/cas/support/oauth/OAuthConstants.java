package org.jasig.cas.support.oauth;

/**
 * This class has the main constants for the OAuth implementation.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public interface OAuthConstants {

    /** OAuth 2 endpoint in CAS. */
    String ENDPOINT_OAUTH2 = "/oauth2.0/*";

    /** OAuth 2 endpoint in CAS. */
    String ENDPOINT_OAUTH2_CALLBACK_AUTHORIZE = "/oauth2.0/callbackAuthorize";

    /** The redirect uri. */
    String REDIRECT_URI = "redirect_uri";

    /** The client id. */
    String CLIENT_ID = "client_id";

    /** The client secret. */
    String CLIENT_SECRET = "client_secret";

    /** The approval prompt. */
    String BYPASS_APPROVAL_PROMPT = "bypass_approval_prompt";

    /** The code. */
    String CODE = "code";

    /** The service. */
    String SERVICE = "service";

    /** The ticket. */
    String TICKET = "ticket";

    /** The state. */
    String STATE = "state";

    /** The access token. */
    String ACCESS_TOKEN = "access_token";

    /** The bearer token. */
    String BEARER_TOKEN = "Bearer";
    
    /** The OAUT h20_ callbackurl. */
    String OAUTH20_CALLBACKURL = "oauth20_callbackUrl";

    /** The OAUT h20_ servic e_ name. */
    String OAUTH20_SERVICE_NAME = "oauth20_service_name";

    /** The OAUT h20_ state. */
    String OAUTH20_STATE = "oauth20_state";

    /**
     * The missing access token.
     **/
    String MISSING_ACCESS_TOKEN = "missing_accessToken";

    /** The expired access token. */
    String EXPIRED_ACCESS_TOKEN = "expired_accessToken";

    /** The confirm view. */
    String CONFIRM_VIEW = "oauthConfirmView";

    /** The error view. */
    String ERROR_VIEW = "serviceErrorView";

    /** The invalid request. */
    String INVALID_REQUEST = "invalid_request";

    /** The invalid grant. */
    String INVALID_GRANT = "invalid_grant";

    /** The authorize url. */
    String AUTHORIZE_URL = "authorize";

    /** The callback authorize url. */
    String CALLBACK_AUTHORIZE_URL = "callbackAuthorize";

    /** The access token url. */
    String ACCESS_TOKEN_URL = "accessToken";

    /** The profile url. */
    String PROFILE_URL = "profile";

    /** The remaining time in seconds before expiration with syntax : expires=3600... */
    String EXPIRES = "expires";
}
