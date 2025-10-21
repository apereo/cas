package org.apereo.cas.oidc.dynareg;

import org.apereo.cas.oidc.jwks.OidcJsonWebKeyStoreJacksonDeserializer;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jose4j.jwk.JsonWebKeySet;
import tools.jackson.databind.annotation.JsonDeserialize;
import java.io.Serial;
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
@Setter
@NoArgsConstructor
public class OidcClientRegistrationRequest implements Serializable {

    @Serial
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
    private String tokenEndpointAuthMethod = OAuth20ClientAuthenticationMethods.CLIENT_SECRET_BASIC.getType();

    @JsonProperty("grant_types")
    private List<String> grantTypes = new ArrayList<>();

    @JsonProperty("response_types")
    private List<String> responseTypes = new ArrayList<>();

    @JsonProperty("jwks_uri")
    private String jwksUri;

    @JsonProperty("userinfo_signed_response_alg")
    private String userInfoSignedResponseAlg;

    @JsonProperty("userinfo_encrypted_response_alg")
    private String userInfoEncryptedResponseAlg;

    @JsonProperty("userinfo_encrypted_response_enc")
    private String userInfoEncryptedResponseEncoding;

    @JsonProperty("jwks")
    @JsonDeserialize(using = OidcJsonWebKeyStoreJacksonDeserializer.class)
    private JsonWebKeySet jwks;

    @JsonProperty("sector_identifier_uri")
    private String sectorIdentifierUri;

    @JsonProperty("contacts")
    private List<String> contacts = new ArrayList<>();

    @JsonProperty("request_object_signing_alg")
    private String requestObjectSigningAlg;

    @JsonProperty("post_logout_redirect_uris")
    private List<String> postLogoutRedirectUris = new ArrayList<>();

    @JsonProperty("introspection_signed_response_alg")
    private String introspectionSignedResponseAlg;

    @JsonProperty("introspection_encrypted_response_alg")
    private String introspectionEncryptedResponseAlg;

    @JsonProperty("introspection_encrypted_response_enc")
    private String introspectionEncryptedResponseEncoding;
    
    @JsonProperty("tls_client_auth_subject_dn")
    private String tlsClientAuthSubjectDn;

    @JsonProperty("tls_client_auth_san_dns")
    private String tlsClientAuthSanDns;

    @JsonProperty("tls_client_auth_san_uri")
    private String tlsClientAuthSanUri;

    @JsonProperty("tls_client_auth_san_ip")
    private String tlsClientAuthSanIp;

    @JsonProperty("tls_client_auth_san_email")
    private String tlsClientAuthSanEmail;

    @JsonProperty("backchannel_token_delivery_mode")
    private String backchannelTokenDeliveryMode;

    @JsonProperty("backchannel_client_notification_endpoint")
    private String backchannelClientNotificationEndpoint;

    @JsonProperty("backchannel_authentication_request_signing_alg")
    private String backchannelAuthenticationRequestSigningAlg;

    @JsonProperty("backchannel_user_code_parameter")
    private boolean backchannelUserCodeParameterSupported;
    
}
