package org.apereo.cas.oidc.discovery;

import org.apereo.cas.oidc.OidcConstants;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
    @JsonProperty
    private final String issuer;

    @JsonProperty("scopes_supported")
    private List<String> scopesSupported;

    @JsonProperty("response_types_supported")
    private List<String> responseTypesSupported;

    @JsonProperty("subject_types_supported")
    private List<String> subjectTypesSupported;

    @JsonProperty("claim_types_supported")
    private List<String> claimTypesSupported;

    @JsonProperty("claims_supported")
    private List<String> claimsSupported;

    @JsonProperty("grant_types_supported")
    private List<String> grantTypesSupported;

    @JsonProperty("id_token_signing_alg_values_supported")
    private List<String> idTokenSigningAlgValuesSupported;

    @JsonProperty("id_token_encryption_alg_values_supported")
    private List<String> idTokenEncryptionAlgValuesSupported;

    @JsonProperty("id_token_encryption_enc_values_supported")
    private List<String> idTokenEncryptionEncodingValuesSupported;

    @JsonProperty("userinfo_signing_alg_values_supported")
    private List<String> userInfoSigningAlgValuesSupported;

    @JsonProperty("userinfo_encryption_alg_values_supported")
    private List<String> userInfoEncryptionAlgValuesSupported;

    @JsonProperty("userinfo_encryption_enc_values_supported")
    private List<String> userInfoEncryptionEncodingValuesSupported;

    @JsonProperty("acr_values_supported")
    private List<String> acrValuesSupported;

    @JsonProperty("request_object_signing_alg_values_supported")
    private List<String> requestObjectSigningAlgValuesSupported;

    @JsonProperty("request_object_encryption_alg_values_supported")
    private List<String> requestObjectEncryptionAlgValuesSupported;

    @JsonProperty("request_object_encryption_enc_values_supported")
    private List<String> requestObjectEncryptionEncodingValuesSupported;

    @JsonProperty("introspection_endpoint_auth_methods_supported")
    private List<String> introspectionSupportedAuthenticationMethods;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("code_challenge_methods_supported")
    private List<String> codeChallengeMethodsSupported;

    @JsonProperty("claims_parameter_supported")
    private boolean claimsParameterSupported = true;

    @JsonProperty("request_uri_parameter_supported")
    private boolean requestUriParameterSupported = true;

    @JsonProperty("request_parameter_supported")
    private boolean requestParameterSupported = true;

    @JsonProperty("authorization_response_iss_parameter_supported")
    private boolean authorizationResponseIssuerParameterSupported = true;

    @JsonProperty("backchannel_logout_supported")
    private boolean backchannelLogoutSupported;

    @JsonProperty("frontchannel_logout_supported")
    private boolean frontchannelLogoutSupported;

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.AUTHORIZE_URL);
    }

    @JsonProperty("token_endpoint")
    public String getTokenEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.ACCESS_TOKEN_URL);
    }

    @JsonProperty("userinfo_endpoint")
    public String getUserinfoEndpoint() {
        return StringUtils.appendIfMissing(this.issuer, "/").concat(OidcConstants.PROFILE_URL);
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
}
