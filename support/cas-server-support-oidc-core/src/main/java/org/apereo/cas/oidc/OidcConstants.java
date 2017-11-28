package org.apereo.cas.oidc;

import org.apache.commons.text.WordUtils;

/**
 * This is {@link OidcConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OidcConstants {

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
     * Standard openid connect scopes.
     */
    enum StandardScopes {
        /**
         * OpenId scope.
         */
        OPENID("openid"),

        /**
         * Custom scope.
         */
        CUSTOM("custom"),

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

        public String getScope() {
            return scope;
        }

        public String getFriendlyName() {
            return WordUtils.capitalize(this.scope.replace('_', ' '));
        }
    }

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
}
