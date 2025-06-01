package org.apereo.cas.oidc.discovery;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * This is {@link OidcServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class OidcServerDiscoverySettings {
    /**
     * Bean name of the factory that creates this instance.
     */
    public static final String BEAN_NAME_FACTORY = "oidcServerDiscoverySettingsFactory";

    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();
    
    @JsonProperty
    private final String issuer;

    @JsonProperty("scopes_supported")
    private Set<String> scopesSupported;

    @JsonProperty("response_types_supported")
    private Set<String> responseTypesSupported;

    @JsonProperty("response_modes_supported")
    private Set<String> responseModesSupported;

    @JsonProperty("subject_types_supported")
    private Set<String> subjectTypesSupported;

    @JsonProperty("claim_types_supported")
    private Set<String> claimTypesSupported;

    @JsonProperty("claims_supported")
    private Set<String> claimsSupported;

    @JsonProperty("grant_types_supported")
    private Set<String> grantTypesSupported;

    @JsonProperty("id_token_signing_alg_values_supported")
    private Set<String> idTokenSigningAlgValuesSupported;

    @JsonProperty("dpop_signing_alg_values_supported")
    private Set<String> dPopSigningAlgValuesSupported;

    @JsonProperty("id_token_encryption_alg_values_supported")
    private Set<String> idTokenEncryptionAlgValuesSupported;

    @JsonProperty("id_token_encryption_enc_values_supported")
    private Set<String> idTokenEncryptionEncodingValuesSupported;

    @JsonProperty("userinfo_signing_alg_values_supported")
    private Set<String> userInfoSigningAlgValuesSupported;

    @JsonProperty("userinfo_encryption_alg_values_supported")
    private Set<String> userInfoEncryptionAlgValuesSupported;

    @JsonProperty("userinfo_encryption_enc_values_supported")
    private Set<String> userInfoEncryptionEncodingValuesSupported;

    @JsonProperty("acr_values_supported")
    private Set<String> acrValuesSupported;

    @JsonProperty("request_object_signing_alg_values_supported")
    private Set<String> requestObjectSigningAlgValuesSupported;

    @JsonProperty("request_object_encryption_alg_values_supported")
    private Set<String> requestObjectEncryptionAlgValuesSupported;

    @JsonProperty("request_object_encryption_enc_values_supported")
    private Set<String> requestObjectEncryptionEncodingValuesSupported;

    @JsonProperty("introspection_endpoint_auth_methods_supported")
    private Set<String> introspectionSupportedAuthenticationMethods;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private Set<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("code_challenge_methods_supported")
    private Set<String> codeChallengeMethodsSupported;

    @JsonProperty("prompt_values_supported")
    private Set<String> promptValuesSupported;
    
    @JsonProperty("claims_parameter_supported")
    private boolean claimsParameterSupported = true;

    @JsonProperty("request_uri_parameter_supported")
    private boolean requestUriParameterSupported = true;

    @JsonProperty("native_sso_supported")
    private boolean nativeSsoSupported = true;

    @JsonProperty("request_parameter_supported")
    private boolean requestParameterSupported = true;

    @JsonProperty("verified_claims_supported")
    private boolean verifiedClaimsSupported = true;

    @JsonProperty("trust_frameworks_supported")
    private Set<String> trustFrameworksSupported;

    @JsonProperty("evidence_supported")
    private Set<String> evidenceSupported;

    @JsonProperty("documents_supported")
    private Set<String> documentsSupported;

    @JsonProperty("documents_validation_methods_supported")
    private Set<String> documentsValidationMethodsSupported;

    @JsonProperty("documents_verification_methods_supported")
    private Set<String> documentsVerificationMethodsSupported;

    @JsonProperty("electronic_records_supported")
    private Set<String> electronicRecordsSupported;

    @JsonProperty("claims_in_verified_claims_supported")
    private Set<String> claimsInVerifiedClaimsSupported;
    
    @JsonProperty("require_pushed_authorization_requests")
    private boolean requirePushedAuthorizationRequests;

    @JsonProperty("authorization_response_iss_parameter_supported")
    private boolean authorizationResponseIssuerParameterSupported = true;

    @JsonProperty("backchannel_logout_supported")
    private boolean backchannelLogoutSupported;

    @JsonProperty("frontchannel_logout_supported")
    private boolean frontchannelLogoutSupported;

    @JsonProperty("tls_client_certificate_bound_access_tokens")
    private boolean tlsClientCertificateBoundAccessTokens;

    @JsonProperty("introspection_signing_alg_values_supported")
    private Set<String> introspectionSignedResponseAlgValuesSupported;

    @JsonProperty("introspection_encryption_alg_values_supported")
    private Set<String> introspectionEncryptedResponseAlgValuesSupported;

    @JsonProperty("introspection_encryption_enc_values_supported")
    private Set<String> introspectionEncryptedResponseEncodingValuesSupported;

    @JsonProperty("backchannel_authentication_request_signing_alg_values_supported")
    private Set<String> backchannelAuthenticationRequestSigningAlgValuesSupported;

    @JsonProperty("backchannel_user_code_parameter_supported")
    private boolean backchannelUserCodeParameterSupported;

    @JsonProperty("backchannel_token_delivery_modes_supported")
    private Set<String> backchannelTokenDeliveryModesSupported;

    @JsonProperty("backchannel_authentication_endpoint")
    public String getBackchannelAuthenticationEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.CIBA_URL);
    }

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.AUTHORIZE_URL);
    }

    @JsonProperty("token_endpoint")
    public String getTokenEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.ACCESS_TOKEN_URL);
    }

    @JsonProperty("device_authorization_endpoint")
    public String getDeviceAuthorizationEndpoint() {
        return getTokenEndpoint();
    }

    @JsonProperty("userinfo_endpoint")
    public String getUserinfoEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.PROFILE_URL);
    }

    @JsonProperty("pushed_authorization_request_endpoint")
    public String getPushedAuthorizationRequestEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.PUSHED_AUTHORIZE_URL);
    }

    @JsonProperty("jwks_uri")
    public String getJwksUri() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.JWKS_URL);
    }

    @JsonProperty("registration_endpoint")
    public String getRegistrationEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.REGISTRATION_URL);
    }

    @JsonProperty("end_session_endpoint")
    public String getEndSessionEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.LOGOUT_URL);
    }

    @JsonProperty("introspection_endpoint")
    public String getIntrospectionEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.INTROSPECTION_URL);
    }

    @JsonProperty("revocation_endpoint")
    public String getRevocationEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.REVOCATION_URL);
    }

    @JsonProperty("backchannel_logout_session_supported")
    public boolean isBackchannelLogoutSessionSupported() {
        return isBackchannelLogoutSupported();
    }

    @JsonProperty("frontchannel_logout_session_supported")
    public boolean isFrontchannelLogoutSessionSupported() {
        return isFrontchannelLogoutSupported();
    }

    /**
     * To JSON string.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return FunctionUtils.doUnchecked(() -> MAPPER.writeValueAsString(this));
    }
}
