package org.apereo.cas.mgmt.services.web.beans;

/**
 * Defines service type for oidc, etc.
 * @author Misagh Moayyed
 * @since 5.0
 */
public class RegisteredServiceOidcTypeEditBean extends RegisteredServiceOAuthTypeEditBean {
    private boolean signToken;
    private String jwks;

    public boolean isSignToken() {
        return signToken;
    }

    public void setSignToken(final boolean signToken) {
        this.signToken = signToken;
    }

    public String getJwks() {
        return jwks;
    }

    public void setJwks(final String jwks) {
        this.jwks = jwks;
    }
}
