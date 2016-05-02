package org.jasig.cas.services;

import org.jasig.cas.support.oauth.services.OAuthRegisteredService;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link OidcRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue("oidc")
public class OidcRegisteredService extends OAuthRegisteredService {

    private static final long serialVersionUID = 1310899699465091444L;

    private String jwks;

    public String getJwks() {
        return jwks;
    }

    public void setJwks(final String jwks) {
        this.jwks = jwks;
    }

    @Override
    public Boolean isJsonFormat() {
        return true;
    }
}
