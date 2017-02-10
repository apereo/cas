package org.apereo.cas.services;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import javax.persistence.Column;
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

    @Column(length = 255, updatable = true, insertable = true)
    private String jwks;

    @Column(updatable = true, insertable = true)
    private boolean signIdToken = true;

    public OidcRegisteredService() {
        setJsonFormat(Boolean.TRUE);
    }

    public boolean isSignIdToken() {
        return signIdToken;
    }

    public void setSignIdToken(final boolean signIdToken) {
        this.signIdToken = signIdToken;
    }

    public String getJwks() {
        return jwks;
    }

    public void setJwks(final String jwks) {
        this.jwks = jwks;
    }
    
    @Override
    protected AbstractRegisteredService newInstance() {
        return new OidcRegisteredService();
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
        final OidcRegisteredService rhs = (OidcRegisteredService) obj;
        final EqualsBuilder builder = new EqualsBuilder();
        return builder
                .appendSuper(super.equals(obj))
                .append(this.jwks, rhs.jwks)
                .append(this.signIdToken, rhs.signIdToken)
                .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(jwks)
                .append(signIdToken)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("jwks", jwks)
                .append("signIdToken", signIdToken)
                .toString();
    }

    @Override
    public void copyFrom(final RegisteredService source) {
        super.copyFrom(source);
        try {
            final OidcRegisteredService oidcService = (OidcRegisteredService) source;
            setJwks(oidcService.getJwks());
            setSignIdToken(oidcService.isSignIdToken());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
