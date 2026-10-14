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
     * @param proofJwt        the compact serialization of the proof JWT
     * @param configurationId the credential configuration the proof is presented for, whose advertised proof
     *                        signing algorithms and cryptographic binding methods the proof must use; null
     *                        accepts any key and algorithm the validator can verify
     * @param consumedNonces  nonces already consumed while handling the current credential request. A request
     *                        carrying several proofs legitimately presents one nonce for all of them, so a
     *                        nonce recorded here is accepted again without being consumed a second time.
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    VerifiableCredentialProofResult validate(String proofJwt, @Nullable String configurationId,
                                             Set<String> consumedNonces)
            throws Exception;

    /**
     * Validate verifiable credential proof result.
     *
     * @param proofJwt the compact serialization of the proof JWT
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    default VerifiableCredentialProofResult validate(final String proofJwt) throws Exception {
        return validate(proofJwt, null, new HashSet<>());
    }

    /**
     * Validate an {@code attestation} proof (OpenID4VCI 1.0 Appendix D): a key attestation whose {@code nonce} is a
     * {@code c_nonce} of this issuer, standing for proof of possession of every key it attests. One proof result is
     * returned per attested key, and one credential is issued for each. Validators that cannot verify key
     * attestations refuse the proof.
     *
     * @param keyAttestation  the key attestation JWT
     * @param configurationId the credential configuration id, or null when the caller names none
     * @param consumedNonces  the nonces already consumed by this request
     * @return the proof results, one per attested key
     * @throws Exception the exception
     */
    default List<VerifiableCredentialProofResult> validateAttestation(final String keyAttestation,
                                                                      final @Nullable String configurationId,
                                                                      final Set<String> consumedNonces)
            throws Exception {
        throw OidcVerifiableCredentialProofException.invalidProof("Attestation proofs are not supported");
    }

    record VerifiableCredentialProofResult(
        String proofType,
        String jwtId,
        String subject,
        JWK holderJwk,
        @Nullable String nonce) {
    }
}
