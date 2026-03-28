package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

/**
 * This is {@link OidcVerifiableCredentialRequest}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OidcVerifiableCredentialRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -700734371623770443L;

    @JsonProperty("credential_configuration_id")
    @NotBlank
    private String credentialConfigurationId;

    /**
     * Optional for later use.
     */
    @JsonProperty("format")
    private String format;

    /**
     * Optional proof object for future expansion.
     */
    @JsonProperty("proof")
    private Proof proof;

    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Proof implements Serializable {
        @Serial
        private static final long serialVersionUID = -6437642481792752853L;

        @JsonProperty("proof_type")
        private String proofType;

        @JsonProperty("jwt")
        private String jwt;
    }
}
