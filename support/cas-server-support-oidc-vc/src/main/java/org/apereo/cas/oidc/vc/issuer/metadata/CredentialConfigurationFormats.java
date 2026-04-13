package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link CredentialConfigurationFormats}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
@Getter
public enum CredentialConfigurationFormats {
    /**
     * (Selective Disclosure JSON Web Token Verifiable Credential) is a
     * privacy-preserving digital credential format based on IETF standards.
     * It allows holders to selectively disclose specific claims—such as revealing
     * only their age rather than their full date of birth—from a signed, trusted
     * credential. It leverages existing JWT infrastructure, offering high
     * compatibility with current identity systems.
     */
    VC_SD_JWT("vc+sd-jwt"),
    /**
     * It is a JSON-based string that represents the credential as a base64url-encoded JWT.
     */
    JWT_VC_JSON("jwt_vc_json");

    private final String format;

}
