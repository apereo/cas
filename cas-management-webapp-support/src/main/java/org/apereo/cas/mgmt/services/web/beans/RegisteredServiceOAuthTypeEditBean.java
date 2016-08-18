package org.apereo.cas.mgmt.services.web.beans;

import java.io.Serializable;

/**
 * Defines service type for OAuth, etc.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class RegisteredServiceOAuthTypeEditBean implements Serializable {
    private static final long serialVersionUID = -3619380614276733103L;

    private String clientSecret;
    private String clientId;
    private boolean bypass;
    private boolean refreshToken;
    private boolean jsonFormat;

    public String getClientSecret() {
        return this.clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public boolean isBypass() {
        return this.bypass;
    }

    public void setBypass(final boolean bypass) {
        this.bypass = bypass;
    }

    public boolean isRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(final boolean refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isJsonFormat() {
        return this.jsonFormat;
    }

    public void setJsonFormat(final boolean jsonFormat) {
        this.jsonFormat = jsonFormat;
    }
}
