package org.apereo.cas.oidc.vc.issuer;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link OidcVerifiableCredentialRequest}, the OpenID4VCI 1.0 credential request.
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

    /**
     * Identifier of the credential dataset the wallet is requesting. Required when the token
     * response returned {@code credential_identifiers} as part of its authorization details,
     * and mutually exclusive with {@link #credentialConfigurationId} in that case.
     */
    @JsonProperty("credential_identifier")
    private @Nullable String credentialIdentifier;

    /**
     * Identifier of the credential configuration the wallet is requesting. Required when the
     * token response did not return {@code credential_identifiers}.
     */
    @JsonProperty("credential_configuration_id")
    private @Nullable String credentialConfigurationId;

    /**
     * Proofs of possession of the key material the issued credentials are bound to. One
     * credential is issued per proof, which is how OpenID4VCI 1.0 expresses batch issuance.
     */
    @JsonProperty("proofs")
    private @Nullable Proofs proofs;

    /**
     * Proof-of-possession material, keyed by proof type.
     */
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class Proofs implements Serializable {
        @Serial
        private static final long serialVersionUID = -6437642481792752853L;

        /**
         * Compact serializations of {@code openid4vci-proof+jwt} proof JWTs.
         */
        @JsonProperty("jwt")
        private List<String> jwt = new ArrayList<>();
    }
}
