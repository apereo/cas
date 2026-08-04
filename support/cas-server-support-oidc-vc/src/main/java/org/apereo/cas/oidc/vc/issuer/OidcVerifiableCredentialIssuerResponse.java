package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialIssuerResponse}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public record OidcVerifiableCredentialIssuerResponse(CredentialConfigurationFormats format, String credential, @Nullable String nonce) {
}
