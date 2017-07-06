package org.apereo.cas.oidc.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.OAuth20Constants;

import java.util.List;

/**
 * This is {@link OidcServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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

    public OidcServerDiscoverySettings(final CasConfigurationProperties casProperties,
                                       final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = casProperties.getServer().getPrefix();
        this.casProperties = casProperties;
    }

    public List<String> getIntrospectionSupportedAuthenticationMethods() {
        return introspectionSupportedAuthenticationMethods;
    }

    public void setIntrospectionSupportedAuthenticationMethods(final List<String> introspectionSupportedAuthenticationMethods) {
        this.introspectionSupportedAuthenticationMethods = introspectionSupportedAuthenticationMethods;
    }

    public String getIssuer() {
        return issuer;
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

    public List<String> getScopesSupported() {
        return scopesSupported;
    }

    public List<String> getResponseTypesSupported() {
        return responseTypesSupported;
    }

    public List<String> getSubjectTypesSupported() {
        return subjectTypesSupported;
    }

    public List<String> getClaimTypesSupported() {
        return claimTypesSupported;
    }

    public List<String> getClaimsSupported() {
        return claimsSupported;
    }

    public void setScopesSupported(final List<String> scopesSupported) {
        this.scopesSupported = scopesSupported;
    }

    public void setResponseTypesSupported(final List<String> responseTypesSupported) {
        this.responseTypesSupported = responseTypesSupported;
    }

    public void setSubjectTypesSupported(final List<String> supportedSubjectResponseTypes) {
        this.subjectTypesSupported = supportedSubjectResponseTypes;
    }

    public void setClaimTypesSupported(final List<String> claimTypesSupported) {
        this.claimTypesSupported = claimTypesSupported;
    }

    public void setClaimsSupported(final List<String> claimsSupported) {
        this.claimsSupported = claimsSupported;
    }

    public List<String> getGrantTypesSupported() {
        return grantTypesSupported;
    }

    public void setGrantTypesSupported(final List<String> grantTypesSupported) {
        this.grantTypesSupported = grantTypesSupported;
    }

    public List<String> getIdTokenSigningAlgValuesSupported() {
        return idTokenSigningAlgValuesSupported;
    }

    public void setIdTokenSigningAlgValuesSupported(final List<String> idTokenSigningAlgValuesSupported) {
        this.idTokenSigningAlgValuesSupported = idTokenSigningAlgValuesSupported;
    }
}
