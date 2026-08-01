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
     * A Data Integrity Claims (DC) credential encoded as an SD-JWT, supporting
     * selective disclosure of claims while preserving cryptographic integrity.
     */
    DC_SD_JWT("dc+sd-jwt"),
    /**
     * It is a JSON-based string that represents the credential as a base64url-encoded JWT.
     */
    JWT_VC_JSON("jwt_vc_json"),
    /**
     * This format includes a {@code @context} URLs that maps data properties to universal schemas.
     * Every field has a strict, globally defined semantic meaning.
     */
    JWT_VC_JSON_LD("jwt_vc_json-ld");

    private final String format;
}
