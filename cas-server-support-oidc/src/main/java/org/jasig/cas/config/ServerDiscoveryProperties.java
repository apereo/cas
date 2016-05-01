package org.jasig.cas.config;

import org.jasig.cas.OidcConstants;
import org.jasig.cas.support.oauth.OAuthConstants;

import java.util.List;

/**
 * This is {@link ServerDiscoveryProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ServerDiscoveryProperties {
    private String issuer;
    private List<String> supportedScopes;
    private List<String> supportedResponseTypes;
    private List<String> supportedSubjectTypes;
    private List<String> supportedClaimTypes;
    private List<String> supportedClaims;
    private List<String> supportedGrantTypes;
    private String serverPrefix;

    public ServerDiscoveryProperties(final String serverPrefix, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = serverPrefix;
    }
    
    public String getIssuer() {
        return issuer;
    }

    public String getAuthorizationEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.AUTHORIZE_URL);
    }

    public String getTokenEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL);
    }

    public String getUserinfoEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.PROFILE_URL);
    }

    public String getJwksEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OidcConstants.JWKS_URL);
    }

    public String getRegistrationEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OidcConstants.REGISTRATION_URL);
    }

    public String getIntrospectionEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OidcConstants.INTROSPECTION_URL);
    }

    public String getRevocationEndpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OidcConstants.REVOCATION_URL);
    }
    
    public List<String> getSupportedScopes() {
        return supportedScopes;
    }

    public List<String> getSupportedResponseTypes() {
        return supportedResponseTypes;
    }

    public List<String> getSupportedSubjectTypes() {
        return supportedSubjectTypes;
    }

    public List<String> getSupportedClaimTypes() {
        return supportedClaimTypes;
    }

    public List<String> getSupportedClaims() {
        return supportedClaims;
    }
    
    public void setSupportedScopes(final List<String> supportedScopes) {
        this.supportedScopes = supportedScopes;
    }

    public void setSupportedResponseTypes(final List<String> supportedResponseTypes) {
        this.supportedResponseTypes = supportedResponseTypes;
    }

    public void setSupportedSubjectTypes(final List<String> supportedSubjectResponseTypes) {
        this.supportedSubjectTypes = supportedSubjectResponseTypes;
    }

    public void setSupportedClaimTypes(final List<String> supportedClaimTypes) {
        this.supportedClaimTypes = supportedClaimTypes;
    }

    public void setSupportedClaims(final List<String> supportedClaims) {
        this.supportedClaims = supportedClaims;
    }

    public List<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public void setSupportedGrantTypes(final List<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
    }
}
