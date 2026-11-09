package org.apereo.cas.configuration.model.support.oidc.federation;

/**
 * Client authentication at a federation endpoint (OpenID Federation 1.0, section 8.8), published for the endpoint as
 * its {@code *_auth_methods} metadata.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public enum OidcFederationEndpointAuthenticationModes {
    /**
     * Requests are not authenticated and use {@code GET}.
     */
    NONE,
    /**
     * Requests either are not authenticated and use {@code GET}, or authenticate with {@code private_key_jwt} and use
     * {@code POST}.
     */
    OPTIONAL,
    /**
     * Requests must authenticate with {@code private_key_jwt} and use {@code POST}.
     */
    REQUIRED
}
