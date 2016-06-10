package org.apereo.cas.configuration.model.support.oidc;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

/**
 * This is {@link OidcProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class OidcProperties {
    
    private String issuer = "http://localhost:8080/cas/oidc";
    private int skew = 5;
    private Resource jwksFile;

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
