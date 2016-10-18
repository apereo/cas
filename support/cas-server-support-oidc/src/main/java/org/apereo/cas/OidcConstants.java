package org.apereo.cas;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

/**
 * This is {@link OidcConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OidcConstants {
    
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
     * The Authorization Server SHOULD prompt the End-User for reauthentication.
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
     * The Scopes.
     */
    List<String> SCOPES = ImmutableList.of("openid", "profile", "email", "address", "phone", "offline_access");

    /**
     * The Claims.
     */
    Set<String> CLAIMS = ImmutableSet.of(CLAIM_SUB, "name", CLAIM_PREFERRED_USERNAME,
            "family_name", "given_name", "middle_name", "given_name", "profile",
            "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
            "birthdate", "email", "email_verified", "phone_number",
            "phone_number_verified", "address");

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
     * Registration endpoint URL.
     */
    String REGISTRATION_URL = "register";

    /**
     * Indicates authentication is required and could not be performed.
     */
    String LOGIN_REQUIRED = "login_required";
}
