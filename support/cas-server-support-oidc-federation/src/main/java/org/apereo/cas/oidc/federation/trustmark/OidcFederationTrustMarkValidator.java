package org.apereo.cas.oidc.federation.trustmark;

import module java.base;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChain;
import com.nimbusds.openid.connect.sdk.federation.trust.TrustChainResolver;

/**
 * This is {@link OidcFederationTrustMarkValidator}. It decides whether an entity whose trust chain was resolved
 * holds the trust marks that are required of it (OpenID Federation 1.0, section 7).
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OidcFederationTrustMarkValidator {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationTrustMarkValidator";

    /**
     * Whether the leaf entity of the trust chain carries a valid trust mark of every required type.
     *
     * @param trustChainResolver the trust chain resolver that resolved the chain
     * @param trustChain         the trust chain of the entity
     * @return true/false
     */
    boolean hasRequiredTrustMarks(TrustChainResolver trustChainResolver, TrustChain trustChain);
}
