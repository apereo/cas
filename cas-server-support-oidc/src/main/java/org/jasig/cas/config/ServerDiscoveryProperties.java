package org.jasig.cas.config;

import org.jasig.cas.OidcConstants;
import org.jasig.cas.support.oauth.OAuthConstants;

import java.util.Set;

/**
 * This is {@link ServerDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ServerDiscoveryProperties {
    private String issuer;
    private Set<String> supportedScopes;
    private Set<String> supportedResponseTypes;
    private Set<String> supportedSubjectTypes;
    private Set<String> supportedClaimTypes;
    private Set<String> supportedClaims;
    private Set<String> supportedGrantTypes;
    private String serverPrefix;

    public ServerDiscoveryProperties(final String serverPrefix, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = serverPrefix;
    }
    
    public String getIssuer() {
        return issuer;
    }

    public String getAuthorizationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.AUTHORIZE_URL);
    }

    public String getTokenEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL);
    }

    public String getUserinfoEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OAuthConstants.PROFILE_URL);
    }

    public String getJwksEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL);
    }

    public String getRegistrationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL);
    }

    public String getIntrospectionEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.INTROSPECTION_URL);
    }

    public String getRevocationEndpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REVOCATION_URL);
    }
    
    public Set<String> getSupportedScopes() {
        return supportedScopes;
    }

    public Set<String> getSupportedResponseTypes() {
        return supportedResponseTypes;
    }

    public Set<String> getSupportedSubjectTypes() {
        return supportedSubjectTypes;
    }

    public Set<String> getSupportedClaimTypes() {
        return supportedClaimTypes;
    }

    public Set<String> getSupportedClaims() {
        return supportedClaims;
    }
    
    public void setSupportedScopes(final Set<String> supportedScopes) {
        this.supportedScopes = supportedScopes;
    }

    public void setSupportedResponseTypes(final Set<String> supportedResponseTypes) {
        this.supportedResponseTypes = supportedResponseTypes;
    }

    public void setSupportedSubjectTypes(final Set<String> supportedSubjectResponseTypes) {
        this.supportedSubjectTypes = supportedSubjectResponseTypes;
    }

    public void setSupportedClaimTypes(final Set<String> supportedClaimTypes) {
        this.supportedClaimTypes = supportedClaimTypes;
    }

    public void setSupportedClaims(final Set<String> supportedClaims) {
        this.supportedClaims = supportedClaims;
    }

    public Set<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(final Set<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }
}
