package org.apereo.cas.config;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.support.oauth.OAuthConstants;

import java.util.List;

/**
 * This is {@link OidcServerDiscoverySettings}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcServerDiscoverySettings {

    private List<String> scopes_supported;
    private List<String> response_types_supported;
    private List<String> subject_types_supported;
    private List<String> claim_types_supported;
    private List<String> claims_supported;
    private List<String> grant_types_supported;

    private final String issuer;
    private final String serverPrefix;

    public OidcServerDiscoverySettings(final String serverPrefix, final String issuer) {
        this.issuer = issuer;
        this.serverPrefix = serverPrefix;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAuthorization_endpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.AUTHORIZE_URL);
    }

    public String getToken_endpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.ACCESS_TOKEN_URL);
    }

    public String getUserinfo_endpoint() {
        return this.serverPrefix.concat(OAuthConstants.BASE_OAUTH20_URL + '/' + OAuthConstants.PROFILE_URL);
    }

    public String getJwks_uri() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.JWKS_URL);
    }

    public String getRegistration_endpoint() {
        return this.serverPrefix.concat('/' + OidcConstants.BASE_OIDC_URL + '/' + OidcConstants.REGISTRATION_URL);
    }

    public List<String> getScopes_supported() {
        return scopes_supported;
    }

    public List<String> getResponse_types_supported() {
        return response_types_supported;
    }

    public List<String> getSubject_types_supported() {
        return subject_types_supported;
    }

    public List<String> getClaim_types_supported() {
        return claim_types_supported;
    }

    public List<String> getClaims_supported() {
        return claims_supported;
    }

    public void setScopes_supported(final List<String> scopes_supported) {
        this.scopes_supported = scopes_supported;
    }

    public void setResponse_types_supported(final List<String> response_types_supported) {
        this.response_types_supported = response_types_supported;
    }

    public void setSubject_types_supported(final List<String> supportedSubjectResponseTypes) {
        this.subject_types_supported = supportedSubjectResponseTypes;
    }

    public void setClaim_types_supported(final List<String> claim_types_supported) {
        this.claim_types_supported = claim_types_supported;
    }

    public void setClaims_supported(final List<String> claims_supported) {
        this.claims_supported = claims_supported;
    }

    public List<String> getGrant_types_supported() {
        return grant_types_supported;
    }

    public void setGrant_types_supported(final List<String> grant_types_supported) {
        this.grant_types_supported = grant_types_supported;
    }
}
