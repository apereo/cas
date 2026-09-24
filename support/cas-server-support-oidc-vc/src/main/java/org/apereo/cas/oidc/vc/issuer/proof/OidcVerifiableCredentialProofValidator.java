package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import com.nimbusds.jose.jwk.JWK;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialProofValidator}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@FunctionalInterface
public interface OidcVerifiableCredentialProofValidator {
    /**
     * Validate verifiable credential proof result.
     *
     * @param proofJwt       the compact serialization of the proof JWT
     * @param consumedNonces nonces already consumed while handling the current credential request. A request
     *                       carrying several proofs legitimately presents one nonce for all of them, so a
     *                       nonce recorded here is accepted again without being consumed a second time.
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    VerifiableCredentialProofResult validate(String proofJwt, Set<String> consumedNonces) throws Exception;

    /**
     * Validate verifiable credential proof result.
     *
     * @param proofJwt the compact serialization of the proof JWT
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    default VerifiableCredentialProofResult validate(final String proofJwt) throws Exception {
        return validate(proofJwt, new HashSet<>());
    }

    record VerifiableCredentialProofResult(
        String proofType,
        String jwtId,
        String subject,
        JWK holderJwk,
        @Nullable String nonce) {
    }
}
