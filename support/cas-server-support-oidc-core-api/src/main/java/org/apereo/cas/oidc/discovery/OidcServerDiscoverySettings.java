package org.apereo.cas.oidc.discovery;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * This is {@link OidcServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
public class OidcServerDiscoverySettings {
    @JsonProperty
    private final String issuer;

    @JsonIgnore
    private final String serverPrefix;

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

    @JsonProperty("introspection_endpoint_auth_methods_supported")
    private List<String> introspectionSupportedAuthenticationMethods;

    @JsonProperty("token_endpoint_auth_methods_supported")
    private List<String> tokenEndpointAuthMethodsSupported;

    @JsonProperty("code_challenge_methods_supported")
    private List<String> codeChallengeMethodsSupported;

    @JsonProperty("claims_parameter_supported")
    private boolean claimsParameterSupported = true;

    @JsonProperty("request_parameter_supported")
    private boolean requestParameterSupported;

    @JsonProperty("backchannel_logout_supported")
    private boolean backchannelLogoutSupported;

    @JsonProperty("frontchannel_logout_supported")
    private boolean frontchannelLogoutSupported;

    public OidcServerDiscoverySettings(final CasConfigurationProperties casProperties, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = casProperties.getServer().getPrefix();
    }

    @JsonProperty("authorization_endpoint")
    public String getAuthorizationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.AUTHORIZE_URL);
    }

    @JsonProperty("token_endpoint")
    public String getTokenEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.ACCESS_TOKEN_URL);
    }

    @JsonProperty("userinfo_endpoint")
    public String getUserinfoEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuth20Constants.PROFILE_URL);
    }

    @JsonProperty("jwks_uri")
    public String getJwksUri() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL);
    }

    @JsonProperty("registration_endpoint")
    public String getRegistrationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL);
    }

    @JsonProperty("end_session_endpoint")
    public String getEndSessionEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.LOGOUT_URL);
    }

    @JsonProperty("introspection_endpoint")
    public String getIntrospectionEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL);
    }

    @JsonProperty("revocation_endpoint")
    public String getRevocationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL);
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
