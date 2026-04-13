package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcVerifiableCredentialValidationContext}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record OidcVerifiableCredentialValidationContext(
    OAuth20AccessToken accessToken,
    OidcVerifiableCredentialRequest credentialRequest,
    HttpServletRequest httpRequest) {
}
