package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link AzureActiveDirectoryCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class AzureActiveDirectoryCredential implements Credential {
    private static final long serialVersionUID = -8899279013317493443L;
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryCredential.class);
    private final String accessToken;
    private final String expiresIn;
    private final String expiresOn;
    private final String resource;
    private final String refreshToken;
    private final String scope;
    private final String notBefore;
    private final String idToken;
    private final Map<String, Object> claims;

    public AzureActiveDirectoryCredential(final String accessToken, final String expiresIn,
                                          final String expiresOn, final String resource, final String refreshToken,
                                          final String scope, final String idToken, final String notBefore) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.expiresOn = expiresOn;
        this.resource = resource;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.idToken = idToken;
        this.notBefore = notBefore;

        final JwtAuthenticator authenticator = new JwtAuthenticator();
        this.claims = authenticator.validateTokenAndGetClaims(this.idToken);
        LOGGER.debug("Claims extracted from ID token are [{}]", this.claims);
    }

    /**
     * Build credential from access token map.
     *
     * @param accessToken the access token
     * @return the azure active directory credential
     */
    public static AzureActiveDirectoryCredential from(final Map<String, String> accessToken) {
        return new AzureActiveDirectoryCredential(accessToken.get("access_token"),
            accessToken.get("expires_in"), accessToken.get("expires_on"), accessToken.get("resource"),
            accessToken.get("refresh_token"), accessToken.get("scope"), accessToken.get("id_token"),
            accessToken.get("not_before"));
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public String getExpiresOn() {
        return expiresOn;
    }

    public String getResource() {
        return resource;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String getIdToken() {
        return idToken;
    }

    /**
     * Gets claims.
     *
     * @return the claims
     */
    public Map<String, Object> getClaims() {
        final Map<String, Object> finalClaims = new HashMap<>(this.claims);
        finalClaims.put("scope", this.scope);
        finalClaims.put("accessToken", this.accessToken);
        finalClaims.put("expiresIn", this.expiresIn);
        finalClaims.put("expiresOn", this.expiresOn);
        finalClaims.put("refreshToken", this.refreshToken);
        finalClaims.put("idToken", this.idToken);
        finalClaims.put("notBefore", this.notBefore);
        finalClaims.put("resource", this.resource);
        return finalClaims;
    }

    @Override
    public String getId() {
        return this.claims.getOrDefault("sub", StringUtils.EMPTY).toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final AzureActiveDirectoryCredential rhs = (AzureActiveDirectoryCredential) obj;
        return new EqualsBuilder()
            .append(this.accessToken, rhs.accessToken)
            .append(this.expiresIn, rhs.expiresIn)
            .append(this.expiresOn, rhs.expiresOn)
            .append(this.resource, rhs.resource)
            .append(this.refreshToken, rhs.refreshToken)
            .append(this.scope, rhs.scope)
            .append(this.idToken, rhs.idToken)
            .append(this.claims, rhs.claims)
            .append(this.notBefore, rhs.notBefore)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(accessToken)
            .append(expiresIn)
            .append(expiresOn)
            .append(resource)
            .append(refreshToken)
            .append(scope)
            .append(idToken)
            .append(claims)
            .append(notBefore)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("accessToken", accessToken)
            .append("expiresIn", expiresIn)
            .append("expiresOn", expiresOn)
            .append("resource", resource)
            .append("refreshToken", refreshToken)
            .append("scope", scope)
            .append("idToken", idToken)
            .append("claims", claims)
            .append("notBefore", notBefore)
            .toString();
    }
}
