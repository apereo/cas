package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.oidc.vc.issuer.proof.OidcVerifiableCredentialProofException;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcVerifiableCredentialValidationContext}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
public record OidcVerifiableCredentialValidationContext(
    OAuth20AccessToken accessToken,
    OidcVerifiableCredentialRequest credentialRequest,
    HttpServletRequest httpRequest) {

    /**
     * Resolve configuration id.
     * <p>
     * A credential identifier issued by CAS is the credential configuration id itself, so either
     * request parameter resolves to the same configuration. When the wallet supplies neither,
     * the configuration recorded on the access token at issuance time is used.
     *
     * @return the string
     */
    public String resolveConfigurationId() {
        if (StringUtils.isNotBlank(credentialRequest.getCredentialIdentifier())) {
            return credentialRequest.getCredentialIdentifier();
        }
        if (StringUtils.isNotBlank(credentialRequest.getCredentialConfigurationId())) {
            return credentialRequest.getCredentialConfigurationId();
        }
        val principal = accessToken.getAuthentication().getPrincipal();
        val recorded = principal.getAttributes().get("credentialConfigurationIds");
        if (recorded == null || recorded.isEmpty()) {
            throw new IllegalArgumentException(
                "Credential request names no credential configuration and the access token records none");
        }
        return recorded.getFirst().toString();
    }

    /**
     * Proof JWTs presented with this request. One credential is issued per proof. When the request carries an
     * {@code attestation} proof instead, there are no proof JWTs.
     *
     * @return the proof JWTs, empty when the request carries an attestation proof
     */
    public List<String> resolveProofs() {
        val proofs = credentialRequest.getProofs();
        val jwts = proofs != null && proofs.getJwt() != null ? proofs.getJwt() : List.<String>of();
        val attestations = proofs != null && proofs.getAttestation() != null ? proofs.getAttestation() : List.<String>of();
        if (!attestations.isEmpty()) {
            if (!jwts.isEmpty() || attestations.size() != 1 || StringUtils.isBlank(attestations.getFirst())) {
                throw OidcVerifiableCredentialProofException.invalidProof(
                    "A credential request carries either proof JWTs or exactly one key attestation");
            }
            return List.of();
        }
        if (jwts.isEmpty() || jwts.stream().anyMatch(StringUtils::isBlank)) {
            throw OidcVerifiableCredentialProofException.invalidProof("Credential request carries no proof of possession");
        }
        return jwts;
    }

    /**
     * Key attestation presented as the {@code attestation} proof, if any.
     *
     * @return the key attestation, or null when the request carries proof JWTs
     */
    public @Nullable String resolveAttestationProof() {
        resolveProofs();
        val proofs = credentialRequest.getProofs();
        return proofs != null && proofs.getAttestation() != null && !proofs.getAttestation().isEmpty()
            ? proofs.getAttestation().getFirst()
            : null;
    }
}
