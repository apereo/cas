package org.apereo.cas.oidc.vc.issuer.metadata;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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

    @JsonProperty("nonce_endpoint")
    private String nonceEndpoint;

    @JsonProperty("batch_credential_endpoint")
    private String batchCredentialEndpoint;

    @JsonProperty("credential_configurations_supported")
    private Map<String, CredentialConfiguration> credentialConfigurationsSupported;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
    public static class CredentialConfiguration implements Serializable {
        @Serial
        private static final long serialVersionUID = 7169398914160552045L;

        @JsonProperty("format")
        private String format = CredentialConfigurationFormats.DC_SD_JWT.getFormat();

        @JsonProperty("scope")
        private String scope;

        @JsonProperty("vct")
        private String vct;

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
    public static class ProofTypeSupported implements Serializable {
        @Serial
        private static final long serialVersionUID = 5908913328617999837L;

        @JsonProperty("proof_signing_alg_values_supported")
        private List<String> proofSigningAlgValuesSupported = Stream.of("ES256", "RS256").toList();
    }

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @Getter
    @Setter
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
        public static class ClaimDisplay implements Serializable {
            @Serial
            private static final long serialVersionUID = 216197021376111795L;

            private String name;
            
            @Builder.Default
            private String locale = "en-US";
        }
    }


}
