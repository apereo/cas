package org.apereo.cas.oidc.jwks;

import org.springframework.core.io.Resource;

/**
 * This is {@link OidcJsonWebKeystoreGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@FunctionalInterface
public interface OidcJsonWebKeystoreGeneratorService {

    /**
     * Generate keystore for OIDC.
     *
     * @return the resource
     */
    Resource generate();
}
