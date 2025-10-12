package org.apereo.cas.oidc.dynareg;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link OidcClientRegistrationResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OidcClientRegistrationResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1436206039117219598L;

    @JsonProperty("jwks")
    private String jwks;

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("client_name")
    private String clientName;

    @JsonProperty("application_type")
    private String applicationType;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("grant_types")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> grantTypes = new ArrayList<>();

    @JsonProperty("logo_uri")
    private String logo;

    @JsonProperty("policy_uri")
    private String policyUri;

    @JsonProperty("tos_uri")
    private String termsOfUseUri;

    @JsonProperty("response_types")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> responseTypes = new ArrayList<>();

    @JsonProperty("redirect_uris")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> redirectUris = new ArrayList<>();

    @JsonProperty("userinfo_signed_response_alg")
    private String userInfoSignedResponseAlg;

    @JsonProperty("userinfo_encrypted_response_alg")
    private String userInfoEncryptedResponseAlg;

    @JsonProperty("userinfo_encrypted_response_enc")
    private String userInfoEncryptedResponseEncoding;

    @JsonProperty("contacts")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private List<String> contacts = new ArrayList<>();

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("token_endpoint_auth_method")
    private String tokenEndpointAuthMethod;

    @JsonProperty("registration_access_token")
    private String registrationAccessToken;

    @JsonProperty("registration_client_uri")
    private String registrationClientUri;
    
    @JsonProperty("client_secret_expires_at")
    private long clientSecretExpiresAt;
    
    @JsonProperty("client_id_issued_at")
    private long clientIdIssuedAt;
}
