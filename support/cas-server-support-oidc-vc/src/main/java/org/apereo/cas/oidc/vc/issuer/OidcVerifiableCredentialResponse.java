package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OidcVerifiableCredentialResponse}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OidcVerifiableCredentialResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = -8698053273429306216L;

    @JsonProperty("format")
    private String format;

    @JsonProperty("credential")
    private String credential;

    @JsonProperty("c_nonce")
    private String cNonce;

    @JsonProperty("c_nonce_expires_at")
    private Long cNonceExpiresAt;
}
