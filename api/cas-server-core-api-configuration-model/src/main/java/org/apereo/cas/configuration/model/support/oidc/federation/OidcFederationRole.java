package org.apereo.cas.configuration.model.support.oidc.federation;

/**
 * This is {@link OidcFederationRole}.
 *
 * @author Jerome LELEU
 * @since 8.0.0
 */
public enum OidcFederationRole {
    /**
     * The root of trust entity issuing statements about other entities.
     */
    TRUST_ANCHOR,
    /**
     * Intermediate entity between the trust anchor and the leaf entities.
     */
    INTERMEDIATE,
    /**
     * Leaf entity playing the OpenID Connect provider protocol role.
     */
    OPENID_PROVIDER
}
