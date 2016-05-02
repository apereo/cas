package org.jasig.cas;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * This is {@link OidcConstants}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OidcConstants {

    /**
     * The Scopes.
     */
    Set<String> SCOPES = ImmutableSet.of("openid", "profile", "email", "address", "phone", "offline_access");

    /**
     * The Claims.
     */
    Set<String> CLAIMS = ImmutableSet.of("sub", "name", "preferred_username",
            "family_name", "given_name", "middle_name", "given_name", "profile",
            "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
            "birthdate", "email", "email_verified", "phone_number",
            "phone_number_verified", "address");

    /**
     * The id token.
     */
    String ID_TOKEN = "id_token";

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
     * The constant INTROSPECTION_URL.
     */
    String INTROSPECTION_URL = "introspect";

    /**
     * Revocation endpoint url.
     */
    String REVOCATION_URL = "revocation";
}

