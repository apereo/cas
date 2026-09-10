package org.apereo.cas.oidc.vc.issuer.proof;

import module java.base;
import org.apereo.cas.oidc.vc.issuer.OidcVerifiableCredentialRequest;
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
     * @param request        the request
     * @param consumedNonces nonces already consumed while handling the current credential request. A batch
     *                       credential request legitimately presents one nonce for all of its entries, so a
     *                       nonce recorded here is accepted again without being consumed a second time.
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    VerifiableCredentialProofResult validate(OidcVerifiableCredentialRequest request,
                                             Set<String> consumedNonces) throws Exception;

    /**
     * Validate verifiable credential proof result.
     *
     * @param request the request
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    default VerifiableCredentialProofResult validate(final OidcVerifiableCredentialRequest request) throws Exception {
        return validate(request, new HashSet<>());
    }

    record VerifiableCredentialProofResult(
        String proofType,
        String jwtId,
        String subject,
        JWK holderJwk,
        @Nullable String nonce) {
    }
}
