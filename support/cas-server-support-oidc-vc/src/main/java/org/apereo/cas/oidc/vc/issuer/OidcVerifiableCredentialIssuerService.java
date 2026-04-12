package org.apereo.cas.oidc.vc.issuer;

import module java.base;

/**
 * This is {@link OidcVerifiableCredentialIssuerService}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface OidcVerifiableCredentialIssuerService {

    /**
     * Issue verifiable credential response.
     *
     * @param context the context
     * @return the verifiable credential response
     */
    OidcVerifiableCredentialResponse issue(OidcVerifiableCredentialValidationContext context);
}
