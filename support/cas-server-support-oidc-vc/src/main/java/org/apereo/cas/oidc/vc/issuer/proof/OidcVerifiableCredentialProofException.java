package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import lombok.Getter;

/**
 * This is {@link OidcVerifiableCredentialProofException}, raised when a proof of possession
 * presented at the credential endpoint cannot be accepted.
 * <p>
 * OpenID4VCI 1.0 separates {@code invalid_proof} from {@code invalid_nonce} for a practical reason:
 * a wallet that is told its nonce is stale asks the nonce endpoint for a fresh one and retries,
 * while a wallet told its proof is invalid has no such recovery. Collapsing both into one error
 * leaves the wallet unable to tell a retryable failure from a terminal one, so the distinction is
 * carried on the exception rather than left to the message text.
 * <p>
 * It extends {@link IllegalArgumentException} because that is what proof validation has always
 * thrown, so existing callers and tests keep working.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Getter
public class OidcVerifiableCredentialProofException extends IllegalArgumentException {
    @Serial
    private static final long serialVersionUID = -4351173390237451892L;

    /**
     * The OpenID4VCI credential error code this failure maps to.
     */
    private final String error;

    public OidcVerifiableCredentialProofException(final String error, final String message) {
        super(message);
        this.error = error;
    }

    /**
     * The proof itself cannot be accepted.
     *
     * @param message the message
     * @return the exception
     */
    public static OidcVerifiableCredentialProofException invalidProof(final String message) {
        return new OidcVerifiableCredentialProofException(OidcConstants.VC_ERROR_INVALID_PROOF, message);
    }

    /**
     * The nonce carried by the proof is missing, unknown, expired or already spent.
     *
     * @param message the message
     * @return the exception
     */
    public static OidcVerifiableCredentialProofException invalidNonce(final String message) {
        return new OidcVerifiableCredentialProofException(OidcConstants.VC_ERROR_INVALID_NONCE, message);
    }
}
