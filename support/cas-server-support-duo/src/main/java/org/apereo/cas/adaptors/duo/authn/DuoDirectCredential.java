package org.apereo.cas.adaptors.duo.authn;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;

import java.io.Serializable;

/**
 * This is {@link DuoDirectCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoDirectCredential implements Credential, Serializable {

    private static final long serialVersionUID = -7570699733132111037L;
    
    private final Authentication authentication;

    public DuoDirectCredential(final Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String getId() {
        return this.authentication.getPrincipal().getId();
    }

    public Authentication getAuthentication() {
        return authentication;
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
        final DuoDirectCredential rhs = (DuoDirectCredential) obj;
        return new EqualsBuilder()
                .append(this.authentication, rhs.authentication)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(authentication)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", authentication)
                .toString();
    }
}
