package org.apereo.cas.oidc.vc.issuer;

/**
 * This is {@link OidcVerifiableCredentialIssuerResponse}, a single credential produced for a
 * single proof of possession. OpenID4VCI 1.0 carries no format on the wire; the format is that
 * of the credential configuration the wallet asked for.
 *
 * @author Misagh Moayyed
 * @param credential the issued credential
 * @since 8.1.0
 */
public record OidcVerifiableCredentialIssuerResponse(String credential) {
}
