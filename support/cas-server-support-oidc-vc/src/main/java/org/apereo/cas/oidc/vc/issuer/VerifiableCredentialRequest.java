package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;

/**
 * This is {@link VerifiableCredentialRequest}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class VerifiableCredentialRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -700734371623770443L;

    @JsonProperty("credential_configuration_id")
    @NotBlank
    private String credentialConfigurationId;
}
