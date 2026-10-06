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
     * Key and algorithms the wallet wants the credential response encrypted with. A request carrying it must itself be
     * encrypted, so that the key cannot be swapped on the way.
     */
    @JsonProperty("credential_response_encryption")
    private @Nullable CredentialResponseEncryption credentialResponseEncryption;

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

        /**
         * Key attestations standing for proof of possession of the keys they attest ({@code attestation} proof type,
         * OpenID4VCI 1.0 Appendix D). Exactly one may be presented, instead of proof JWTs.
         */
        @JsonProperty("attestation")
        private List<String> attestation = new ArrayList<>();
    }

    /**
     * Parameters for encrypting the credential response (OpenID4VCI 1.0 section 8.2).
     */
    @Getter
    @Setter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CredentialResponseEncryption implements Serializable {
        @Serial
        private static final long serialVersionUID = 3870158423615027931L;

        /**
         * The public key, as a JWK, the response is encrypted to. Its {@code alg} names the JWE key management algorithm.
         */
        @JsonProperty("jwk")
        private @Nullable Map<String, Object> jwk;

        /**
         * The JWE content encryption algorithm.
         */
        @JsonProperty("enc")
        private @Nullable String enc;

        /**
         * The JWE compression algorithm, which CAS does not support.
         */
        @JsonProperty("zip")
        private @Nullable String zip;
    }
}
