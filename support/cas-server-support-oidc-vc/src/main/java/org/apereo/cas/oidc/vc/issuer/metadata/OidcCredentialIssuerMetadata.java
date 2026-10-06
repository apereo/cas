package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * This is {@link OidcCredentialIssuerMetadata}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter
@Setter
@NoArgsConstructor
public class OidcCredentialIssuerMetadata implements Serializable {
    @Serial
    private static final long serialVersionUID = -7403145313503329287L;

    @JsonProperty("credential_issuer")
    private String credentialIssuer;

    @JsonProperty("authorization_servers")
    private List<String> authorizationServers;

    @JsonProperty("credential_endpoint")
    private String credentialEndpoint;

    @JsonProperty("display")
    private List<CredentialConfigurationDisplay> display;

    @JsonProperty("nonce_endpoint")
    private String nonceEndpoint;

    @JsonProperty("notification_endpoint")
    private String notificationEndpoint;

    @JsonProperty("credential_request_encryption")
    private CredentialRequestEncryption credentialRequestEncryption;

    @JsonProperty("credential_response_encryption")
    private CredentialResponseEncryption credentialResponseEncryption;

    @JsonProperty("batch_credential_issuance")
    private BatchCredentialIssuance batchCredentialIssuance;

    @JsonProperty("credential_configurations_supported")
    private Map<String, CredentialConfiguration> credentialConfigurationsSupported;

    /**
     * Advertises that the credential endpoint accepts encrypted requests, and the keys to encrypt them to
     * (OpenID4VCI 1.0 section 12.2.4). {@code encryption_required} is always present, since the specification requires it.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class CredentialRequestEncryption implements Serializable {
        @Serial
        private static final long serialVersionUID = 2915368745201936482L;

        @JsonProperty("jwks")
        private Map<String, Object> jwks;

        @JsonProperty("enc_values_supported")
        private List<String> encValuesSupported;

        @JsonProperty("encryption_required")
        @JsonInclude(JsonInclude.Include.ALWAYS)
        private boolean encryptionRequired;
    }

    /**
     * Advertises that the credential endpoint encrypts responses on request, and with which algorithms
     * (OpenID4VCI 1.0 section 12.2.4). {@code encryption_required} is always present, since the specification requires it.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class CredentialResponseEncryption implements Serializable {
        @Serial
        private static final long serialVersionUID = 5018273649120374851L;

        @JsonProperty("alg_values_supported")
        private List<String> algValuesSupported;

        @JsonProperty("enc_values_supported")
        private List<String> encValuesSupported;

        @JsonProperty("encryption_required")
        @JsonInclude(JsonInclude.Include.ALWAYS)
        private boolean encryptionRequired;
    }

    /**
     * Advertises that the credential endpoint accepts several proofs in one request,
     * and how many. OpenID4VCI 1.0 replaced the batch credential endpoint with this.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class BatchCredentialIssuance implements Serializable {
        @Serial
        private static final long serialVersionUID = 7169398914160552046L;

        @JsonProperty("batch_size")
        private int batchSize;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class CredentialDefinition implements Serializable {
        @Serial
        private static final long serialVersionUID = 3316840958171736511L;

        @JsonProperty("@context")
        private List<String> context;

        @JsonProperty("type")
        private List<String> type;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    public static class CredentialConfiguration implements Serializable {
        @Serial
        private static final long serialVersionUID = 7169398914160552045L;

        @JsonProperty("format")
        private String format = CredentialConfigurationFormats.DC_SD_JWT.getValue();

        @JsonProperty("scope")
        private String scope;

        @JsonProperty("vct")
        private String vct;

        @JsonProperty("credential_definition")
        private CredentialDefinition credentialDefinition;

        @JsonProperty("cryptographic_binding_methods_supported")
        private List<String> cryptographicBindingMethodsSupported = Stream.of("jwk").toList();

        @JsonProperty("credential_signing_alg_values_supported")
        private List<String> credentialSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

        @JsonProperty("proof_types_supported")
        private Map<String, ProofTypeSupported> proofTypesSupported = new LinkedHashMap<>();

        @JsonProperty("credential_metadata")
        private CredentialMetadata credentialMetadata = new CredentialMetadata();
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class CredentialMetadata implements Serializable {
        @Serial
        private static final long serialVersionUID = 7269398914160552045L;

        @JsonProperty("display")
        private List<CredentialConfigurationDisplay> display;

        @JsonProperty("claims")
        private List<ClaimMetadata> claims;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class ProofTypeSupported implements Serializable {
        @Serial
        private static final long serialVersionUID = 5908913328617999837L;

        @JsonProperty("proof_signing_alg_values_supported")
        @Builder.Default
        private List<String> proofSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();

        /**
         * Key attestations required with proofs of this type; present, even empty, means a key attestation is required,
         * so an empty object is still written.
         */
        @JsonProperty("key_attestations_required")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private KeyAttestationsRequired keyAttestationsRequired;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class KeyAttestationsRequired implements Serializable {
        @Serial
        private static final long serialVersionUID = 3108913328617999837L;

        /**
         * Accepted key storage attack potential resistance levels; a key attestation must name at least one.
         */
        @JsonProperty("key_storage")
        private List<String> keyStorage;

        /**
         * Accepted user authentication attack potential resistance levels; a key attestation must name at least one.
         */
        @JsonProperty("user_authentication")
        private List<String> userAuthentication;
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    @NoArgsConstructor
    @SuperBuilder
    @Jacksonized
    public static class ClaimMetadata implements Serializable {
        @Serial
        private static final long serialVersionUID = 216197021376111794L;

        @JsonProperty("path")
        private List<String> path;

        @JsonProperty("display")
        private List<ClaimDisplay> display;
        
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Getter
        @Setter
        @SuperBuilder
        @Jacksonized
        @NoArgsConstructor
        public static class ClaimDisplay implements Serializable {
            @Serial
            private static final long serialVersionUID = 216197021376111795L;

            private String name;
            
            @Builder.Default
            private String locale = "en-US";
        }
    }


}
