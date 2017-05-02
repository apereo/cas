package org.apereo.cas.configuration.model.support.oidc;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OidcProperties {

    private int jwksCacheInMinutes = 60;
    private String issuer = "http://localhost:8080/cas/oidc";
    private int skew = 5;
    private Resource jwksFile = new FileSystemResource("/etc/cas/keystore.jwks");
    private String dynamicClientRegistrationMode;
    private List<String> scopes = Arrays.asList("openid", "profile", "email", "address", "phone", "offline_access");
    private List<String> claims = Arrays.asList("sub", "name", "preferred_username",
            "family_name", "given_name", "middle_name", "given_name", "profile",
            "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
            "birthdate", "email", "email_verified", "phone_number",
            "phone_number_verified", "address");
    private List<String> subjectTypes = Arrays.asList("public");

    private Map<String, String> userDefinedScopes = new HashMap<>();
    private Map<String, String> claimsMap = new HashMap<>();

    public Map<String, String> getClaimsMap() {
        return claimsMap;
    }

    public void setClaimsMap(final Map<String, String> claimsMap) {
        this.claimsMap = claimsMap;
    }

    public Map<String, String> getUserDefinedScopes() {
        return userDefinedScopes;
    }

    public void setUserDefinedScopes(final Map<String, String> userDefinedScopes) {
        this.userDefinedScopes = userDefinedScopes;
    }

    public int getJwksCacheInMinutes() {
        return jwksCacheInMinutes;
    }

    public void setJwksCacheInMinutes(final int jwksCacheInMinutes) {
        this.jwksCacheInMinutes = jwksCacheInMinutes;
    }

    public List<String> getSubjectTypes() {
        return subjectTypes;
    }

    public void setSubjectTypes(final List<String> subjectTypes) {
        this.subjectTypes = subjectTypes;
    }

    public List<String> getClaims() {
        return claims;
    }

    public void setClaims(final List<String> claims) {
        this.claims = claims;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public void setScopes(final List<String> scopes) {
        this.scopes = scopes;
    }

    public String getDynamicClientRegistrationMode() {
        return dynamicClientRegistrationMode;
    }

    public void setDynamicClientRegistrationMode(final String dynamicClientRegistrationMode) {
        this.dynamicClientRegistrationMode = dynamicClientRegistrationMode;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public int getSkew() {
        return skew;
    }

    public void setSkew(final int skew) {
        this.skew = skew;
    }

    public Resource getJwksFile() {
        return jwksFile;
    }

    public void setJwksFile(final Resource jwksFile) {
        this.jwksFile = jwksFile;
    }
}
