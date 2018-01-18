package org.apereo.cas.oidc.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link OidcServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class OidcServerDiscoverySettings {

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

    @JsonProperty("introspection_endpoint_auth_methods_supported")
    private List<String> introspectionSupportedAuthenticationMethods;

    private final CasConfigurationProperties casProperties;

    private final String issuer;

    private final String serverPrefix;

    public OidcServerDiscoverySettings(final CasConfigurationProperties casProperties, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = casProperties.getServer().getPrefix();
        this.casProperties = casProperties;
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
        return casProperties.getServer().getLogoutUrl();
    }

    @JsonProperty("introspection_endpoint")
    public String getIntrospectionEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL);
    }
}
