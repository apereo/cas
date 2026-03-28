package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import jakarta.servlet.http.HttpServletRequest;

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
    OidcVerifiableCredentialResponse issue(CredentialRequestValidationContext context);

    record CredentialRequestValidationContext(
        OAuth20AccessToken accessToken,
        OidcVerifiableCredentialRequest credentialRequest,
        HttpServletRequest httpRequest) {
    }
}
