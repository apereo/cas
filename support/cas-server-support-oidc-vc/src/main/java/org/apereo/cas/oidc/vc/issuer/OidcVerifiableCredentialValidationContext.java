package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
        return principal.getAttributes().get("credentialConfigurationIds").getFirst().toString();
    }

    /**
     * Proof JWTs presented with this request. One credential is issued per proof.
     *
     * @return the proof JWTs, never empty
     */
    public List<String> resolveProofs() {
        val proofs = credentialRequest.getProofs();
        val jwts = proofs != null && proofs.getJwt() != null ? proofs.getJwt() : List.<String>of();
        if (jwts.isEmpty() || jwts.stream().anyMatch(StringUtils::isBlank)) {
            throw new IllegalArgumentException("Credential request carries no proof of possession");
        }
        return jwts;
    }
}
