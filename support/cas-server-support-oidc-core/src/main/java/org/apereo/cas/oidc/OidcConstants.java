package org.apereo.cas.oidc;

import lombok.Getter;
import org.apache.commons.text.WordUtils;

/**
 * This is {@link OidcConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OidcConstants {

    /**
     * The token.
     */
    String TOKEN = "token";
    /**
     * ACR passed in the id token.
     */
    String ACR = "acr";
    /**
     * Authentication method reference passed in the id token.
     */
    String AMR = "amr";
    /**
     * The Authorization Server MUST NOT display any authentication or consent user interface pages.
     */
    String PROMPT_NONE = "none";
    /**
     * The Authorization Server SHOULD prompt the End-User for re-authentication.
     */
    String PROMPT_LOGIN = "login";
    /**
     * The Authorization Server SHOULD prompt the End-User consent.
     */
    String PROMPT_CONSENT = "consent";
    /**
     * The sub claim.
     */
    String CLAIM_SUB = "sub";
    /**
     * The preferred username claim.
     */
    String CLAIM_PREFERRED_USERNAME = "preferred_username";
    /**
     * The authentication time claim.
     */
    String CLAIM_AUTH_TIME = "auth_time";
    /**
     * The access token hash.
     */
    String CLAIM_AT_HASH = "at_hash";
    /**
     * The session identifier claim.
     */
    String CLAIM_SESSIOND_ID = "sid";
    /**
     * The id token.
     */
    String ID_TOKEN = "id_token";
    /**
     * The max age.
     */
    String MAX_AGE = "max_age";
    /**
     * The prompt parameter.
     */
    String PROMPT = "prompt";
    /**
     * Base OIDC URL.
     */
    String BASE_OIDC_URL = "oidc";

    /**
     * Logout url.
     */
    String LOGOUT_URL = "logout";
    /**
     * JWKS Endpoint url.
     */
    String JWKS_URL = "jwks";
    /**
     * Revocation Endpoint url.
     */
    String REVOCATION_URL = "revoke";
    /**
     * Registration endpoint URL.
     */
    String REGISTRATION_URL = "register";
    /**
     * Client configuration endpoint URL.
     */
    String CLIENT_CONFIGURATION_URL = "clientConfig";
    /**
     * The introspection url.
     */
    String INTROSPECTION_URL = "introspect";
    /**
     * Indicates authentication is required and could not be performed.
     */
    String LOGIN_REQUIRED = "login_required";
    /**
     * The confirm/consent view.
     */
    String CONFIRM_VIEW = "oidcConfirmView";
    /**
     * Rel value for webfinger protocol.
     */
    String WEBFINGER_REL = "http://openid.net/specs/connect/1.0/issuer";

    /**
     * Scope assigned to access token internally
     * to access client config urls and look up relying parties.
     */
    String CLIENT_REGISTRATION_SCOPE = "ClientRegistrationScope";

    /**
     * Parameter used to look up clients by their id.
     */
    String CLIENT_REGISTRATION_CLIENT_ID = "clientId";

    /**
     * Authenticator used to verify access to client configuration endpoint.
     */
    String CAS_OAUTH_CLIENT_CONFIG_ACCESS_TOKEN_AUTHN = "ClientRegistrationClient";
    /**
     * Authenticator used to verify access using private key jwts.
     */
    String CAS_OAUTH_CLIENT_PRIVATE_KEY_JWT_AUTHN = "ClientPrivateKeyJwtClient";
    /**
     * Authenticator used to verify access using client secret jwts.
     */
    String CAS_OAUTH_CLIENT_CLIENT_SECRET_JWT_AUTHN = "ClientSecretJwtClient";
    /**
     * This is a standard label for a custom scope which will have a scope name.
     * This should not be added to StandardScopes enumeration because it isn't standard.
     */
    String CUSTOM_SCOPE_TYPE = "custom";
    /**
     * JWT content type.
     */
    String CONTENT_TYPE_JWT = "application/jwt";

    /**
     * Dynamic client registration mode.
     */
    enum DynamicClientRegistrationMode {

        /**
         * Registration is open to all.
         */
        OPEN,
        /**
         * registration is protected for all.
         */
        PROTECTED
    }

    /**
     * Standard openid connect scopes.
     */
    @Getter
    enum StandardScopes {

        /**
         * OpenId scope.
         */
        OPENID("openid"),
        /**
         * address scope.
         */
        ADDRESS("address"),
        /**
         * email scope.
         */
        EMAIL("email"),
        /**
         * profile scope.
         */
        PROFILE("profile"),
        /**
         * phone scope.
         */
        PHONE("phone"),
        /**
         * offline_access scope.
         */
        OFFLINE_ACCESS("offline_access");

        private final String scope;

        StandardScopes(final String scope) {
            this.scope = scope;
        }

        public String getFriendlyName() {
            return WordUtils.capitalize(this.scope.replace('_', ' '));
        }
    }
}
