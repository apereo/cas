package org.apereo.cas.oidc.federation.chain;

import module java.base;
import org.apereo.cas.services.OidcRegisteredService;

/**
 * This is {@link OidcFederationTrustChainResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface OidcFederationTrustChainResolver {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "oidcFederationTrustChainResolver";

    /**
     * Resolve trust chains.
     *
     * @param clientId the client id
     * @return the optional
     * @throws Exception the exception
     */
    Optional<OidcRegisteredService> resolveTrustChains(String clientId) throws Exception;

    /**
     * Register a relying party from the trust chain it sent, such as the {@code trust_chain} header of its request
     * object (OpenID Federation 1.0, section 12.1.1.1), once the chain is fully verified, or resolve its trust chains
     * when the chain cannot be used.
     *
     * @param clientId   the client id
     * @param trustChain the serialized statements of the trust chain the relying party sent, which may be empty
     * @return the optional
     * @throws Exception the exception
     */
    default Optional<OidcRegisteredService> resolveTrustChains(final String clientId, final List<String> trustChain) throws Exception {
        return resolveTrustChains(clientId);
    }
}
