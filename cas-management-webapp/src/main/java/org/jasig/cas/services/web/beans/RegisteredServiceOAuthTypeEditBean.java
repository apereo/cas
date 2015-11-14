package org.jasig.cas.services.web.beans;


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

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(final String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    public boolean isBypass() {
        return bypass;
    }

    public void setBypass(final boolean bypass) {
        this.bypass = bypass;
    }
}
