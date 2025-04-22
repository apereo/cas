package org.apereo.cas.oidc.federation;

import org.apereo.cas.services.OidcRegisteredService;
import java.util.Optional;

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
}
