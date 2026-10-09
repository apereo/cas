package org.apereo.cas.oidc.vc.issuer.encryption;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import lombok.Getter;

/**
 * This is {@link OidcVerifiableCredentialEncryptionException}, raised when an encrypted credential request cannot be
 * read, or the parameters a wallet supplied for encrypting the credential response cannot be used. It carries the
 * OpenID4VCI error code the credential endpoint answers with.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialEncryptionException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = 6170483950217846352L;

    /**
     * The OpenID4VCI credential error code this failure maps to.
     */
    private final String error;

    public OidcVerifiableCredentialEncryptionException(final String error, final String message) {
        super(message);
        this.error = error;
    }

    /**
     * The encrypted credential request cannot be decrypted or read.
     *
     * @param message the message
     * @return the exception
     */
    public static OidcVerifiableCredentialEncryptionException invalidRequest(final String message) {
        return new OidcVerifiableCredentialEncryptionException(OidcConstants.VC_ERROR_INVALID_CREDENTIAL_REQUEST, message);
    }

    /**
     * The {@code credential_response_encryption} parameters are invalid, or missing while required.
     *
     * @param message the message
     * @return the exception
     */
    public static OidcVerifiableCredentialEncryptionException invalidParameters(final String message) {
        return new OidcVerifiableCredentialEncryptionException(OidcConstants.VC_ERROR_INVALID_ENCRYPTION_PARAMETERS, message);
    }
}
