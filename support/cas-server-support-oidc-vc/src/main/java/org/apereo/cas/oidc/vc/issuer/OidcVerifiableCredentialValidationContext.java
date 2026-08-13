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
     *
     * @return the string
     */
    public String resolveConfigurationId() {
        val principal = accessToken.getAuthentication().getPrincipal();
        return StringUtils.isBlank(credentialRequest.getCredentialConfigurationId())
            ? principal.getAttributes().get("credentialConfigurationIds").getFirst().toString()
            : credentialRequest.getCredentialConfigurationId();
    }
}
