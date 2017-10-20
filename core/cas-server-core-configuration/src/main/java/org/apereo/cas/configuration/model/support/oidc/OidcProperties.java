package org.apereo.cas.configuration.model.support.oidc;

import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-support-oidc")
public class OidcProperties implements Serializable {

    private static final long serialVersionUID = 813028615694269276L;
    /**
     * Timeout that indicates how long should the JWKS file be kept in cache.
     */
    private int jwksCacheInMinutes = 60;
    /**
     * OIDC issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas/oidc";
    /**
     * Skew value used to massage the authentication issue instance.
     */
    private int skew = 5;
    /**
     * Path to the JWKS file resource used to handle signing/encryption of authentication tokens.
     */
    @RequiredProperty
    private Resource jwksFile = new FileSystemResource("/etc/cas/keystore.jwks");
    /**
     * Whether dynamic registration operates in {@code OPEN} or {@code PROTECTED} mode.
     */
    private String dynamicClientRegistrationMode;
    /**
     * List of supported scopes.
     */
    private List<String> scopes = CollectionUtils.wrapList("openid", "profile", "email", "address", "phone", "offline_access");
    /**
     * List of supported claims.
     */
    private List<String> claims = CollectionUtils.wrapList("sub", "name", "preferred_username",
            "family_name", "given_name", "middle_name", "given_name", "profile",
            "picture", "nickname", "website", "zoneinfo", "locale", "updated_at",
            "birthdate", "email", "email_verified", "phone_number",
            "phone_number_verified", "address");

    /**
     * List of supported subject types.
     */
    private List<String> subjectTypes = CollectionUtils.wrapList("public", "pairwise");

    /**
     * Mapping of user-defined scopes. Key is the new scope name
     * and value is a comma-separated list of claims mapped to the scope.
     */
    private Map<String, String> userDefinedScopes = new HashMap<>();
    /**
     * Map fixed claims to CAS attributes.
     * Key is the existing claim name for a scope and value is the new attribute
     * that should take its place and value.
     */
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
