package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcClientRegistrationRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
@Getter
@NoArgsConstructor
public class OidcClientRegistrationRequest implements Serializable {

    private static final long serialVersionUID = 1832102135613155844L;

    @JsonProperty("redirect_uris")
    private List<String> redirectUris = new ArrayList<>();

    @JsonProperty("default_acr_values")
    private List<String> defaultAcrValues = new ArrayList<>();

    @JsonProperty("registration_client_uri")
    private String registrationClientUri;
    
    @JsonProperty("id_token_signed_response_alg")
    private String idTokenSignedResponseAlg;

    @JsonProperty("id_token_encrypted_response_alg")
    private String idTokenEncryptedResponseAlg;

    @JsonProperty("id_token_encrypted_response_enc")
    private String idTokenEncryptedResponseEncoding;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("application_type")
    private String applicationType;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("logo_uri")
    private String logo;

    @JsonProperty("policy_uri")
    private String policyUri;

    @JsonProperty("tos_uri")
    private String termsOfUseUri;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("grant_types")
    private List<String> grantTypes = new ArrayList<>();

    @JsonProperty("response_types")
    private List<String> responseTypes = new ArrayList<>();

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("sector_identifier_uri")
    private String sectorIdentifierUri;

    @JsonProperty("contacts")
    private List<String> contacts = new ArrayList<>();

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("post_logout_redirect_uris")
    private List<String> postLogoutRedirectUris = new ArrayList<>();
}
