package org.apereo.cas.mgmt.services.web.beans;

/**
 * Defines service type for oidc, etc.
 *
 * @author Misagh Moayyed
 * @since 5.0
 */
public class RegisteredServiceOidcTypeEditBean extends RegisteredServiceOAuthTypeEditBean {
    private static final long serialVersionUID = -378685014926798349L;
    private boolean signToken = true;
    private String jwks;
    private boolean implicit;
    private boolean encrypt;
    private String encryptAlg;
    private String encryptEnc;
    private String dynamicDate;
    private boolean dynamic;
    private String scopes;

    public String getScopes() {
        return scopes;
    }

    public void setScopes(final String scopes) {
        this.scopes = scopes;
    }

    public String getDynamicDate() {
        return dynamicDate;
    }

    public void setDynamicDate(final String dynamicDate) {
        this.dynamicDate = dynamicDate;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(final boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isEncrypt() {
        return encrypt;
    }

    public void setEncrypt(final boolean encrypt) {
        this.encrypt = encrypt;
    }

    public String getEncryptAlg() {
        return encryptAlg;
    }

    public void setEncryptAlg(final String encryptAlg) {
        this.encryptAlg = encryptAlg;
    }

    public String getEncryptEnc() {
        return encryptEnc;
    }

    public void setEncryptEnc(final String encryptEnc) {
        this.encryptEnc = encryptEnc;
    }

    public boolean isImplicit() {
        return implicit;
    }

    public void setImplicit(final boolean implicit) {
        this.implicit = implicit;
    }

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
