package org.apereo.cas.oidc.vc.issuer.enc;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialValidationContext;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofValidator;

/**
 * This is {@link OidcVerifiableCredentialEncoder}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
public interface OidcVerifiableCredentialEncoder {

    /**
     * Gets format.
     *
     * @return the format
     */
    OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats getFormat();

    /**
     * Encode.
     *
     * @param context the context
     * @param proof   the proof
     * @return the signed credential
     * @throws Throwable the throwable
     */
    String encode(OidcVerifiableCredentialValidationContext context,
                  OidcVerifiableCredentialProofValidator.VerifiableCredentialProofResult proof) throws Throwable;
}
