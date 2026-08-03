package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialIssuerResponse}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public record OidcVerifiableCredentialIssuerResponse(String format, String credential, @Nullable String nonce) {
}
