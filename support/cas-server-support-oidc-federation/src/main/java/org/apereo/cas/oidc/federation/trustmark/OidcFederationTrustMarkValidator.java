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

    /**
     * The trust marks the leaf entity of the trust chain publishes that are valid, whatever their type: the trust
     * anchor of the chain recognizes their issuer for their type, and they are active. A trust mark whose validity
     * cannot be told now is left out, and the result says that it is incomplete.
     *
     * @param trustChainResolver the trust chain resolver that resolved the chain
     * @param trustChain         the trust chain of the entity
     * @return the valid trust marks
     */
    default ValidTrustMarks resolveValidTrustMarks(final TrustChainResolver trustChainResolver, final TrustChain trustChain) {
        return new ValidTrustMarks(List.of(), true);
    }

    /**
     * The valid trust marks of an entity.
     *
     * @param trustMarks the valid trust marks, each with its {@code trust_mark_type} and {@code trust_mark}
     * @param complete   whether the validity of every trust mark of the entity could be told, so that none was left out
     *                   for that reason
     */
    record ValidTrustMarks(List<Map<String, Object>> trustMarks, boolean complete) {
    }
}
