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
     * @param context        the context
     * @param consumedNonces nonces already consumed while handling the current credential request
     * @return the verifiable credential response
     * @throws Throwable the throwable
     */
    List<OidcVerifiableCredentialIssuerResponse> issue(OidcVerifiableCredentialValidationContext context,
                                                       Set<String> consumedNonces) throws Throwable;

    /**
     * Issue verifiable credential response.
     *
     * @param context the context
     * @return the verifiable credential response
     * @throws Throwable the throwable
     */
    default List<OidcVerifiableCredentialIssuerResponse> issue(
        final OidcVerifiableCredentialValidationContext context) throws Throwable {
        return issue(context, new HashSet<>());
    }
}
