package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.nimbusds.jose.jwk.JWK;

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
     * @param request the request
     * @return the verifiable credential proof result
     * @throws Exception the exception
     */
    VerifiableCredentialProofResult validate(VerifiableCredentialRequest request) throws Exception;

    record VerifiableCredentialProofResult(
        String proofType,
        String jwtId,
        String subject,
        JWK holderJwk) {
    }
}
