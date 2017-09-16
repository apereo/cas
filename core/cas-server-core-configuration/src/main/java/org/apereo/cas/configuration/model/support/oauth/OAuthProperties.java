package org.apereo.cas.configuration.model.support.oauth;

import java.io.Serializable;

/**
 * This is {@link OAuthProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuthProperties implements Serializable {
    private static final long serialVersionUID = 2677128037234123907L;
    /**
     * Settings related to oauth grants.
     */
    private OAuthGrantsProperties grants = new OAuthGrantsProperties();
    /**
     * Settings related to oauth codes.
     */
    private OAuthCodeProperties code = new OAuthCodeProperties();
    /**
     * Settings related to oauth access tokens.
     */
    private OAuthAccessTokenProperties accessToken = new OAuthAccessTokenProperties();
    /**
     * Settings related to oauth refresh tokens.
     */
    private OAuthRefreshTokenProperties refreshToken = new OAuthRefreshTokenProperties();

    public OAuthGrantsProperties getGrants() {
        return grants;
    }

    public void setGrants(final OAuthGrantsProperties grants) {
        this.grants = grants;
    }

    public OAuthAccessTokenProperties getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final OAuthAccessTokenProperties accessToken) {
        this.accessToken = accessToken;
    }

    public OAuthRefreshTokenProperties getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(final OAuthRefreshTokenProperties refreshToken) {
        this.refreshToken = refreshToken;
    }

    public OAuthCodeProperties getCode() {
        return code;
    }

    public void setCode(final OAuthCodeProperties code) {
        this.code = code;
    }
}

