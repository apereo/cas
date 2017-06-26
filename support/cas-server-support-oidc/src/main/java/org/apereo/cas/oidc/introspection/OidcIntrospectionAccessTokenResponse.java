package org.apereo.cas.oidc.introspection;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * This is {@link OidcIntrospectionAccessTokenResponse}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OidcIntrospectionAccessTokenResponse {

    private boolean active;
    private String sub;
    private String scope;
    private long iat;
    private long exp;
    private String realmName;
    private String uniqueSecurityName;
    private String tokenType;
    private String aud;
    private String iss;
    
    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("grant_type")
    private String grantType;


    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public String getSub() {
        return sub;
    }

    public void setSub(final String sub) {
        this.sub = sub;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(final String scope) {
        this.scope = scope;
    }

    public long getIat() {
        return iat;
    }

    public void setIat(final long iat) {
        this.iat = iat;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(final long exp) {
        this.exp = exp;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(final String realmName) {
        this.realmName = realmName;
    }

    public String getUniqueSecurityName() {
        return uniqueSecurityName;
    }

    public void setUniqueSecurityName(final String uniqueSecurityName) {
        this.uniqueSecurityName = uniqueSecurityName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(final String tokenType) {
        this.tokenType = tokenType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(final String grantType) {
        this.grantType = grantType;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(final String aud) {
        this.aud = aud;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(final String iss) {
        this.iss = iss;
    }
}
