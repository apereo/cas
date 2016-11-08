package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Base class for CAS credentials that are safe for long-term storage.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public abstract class AbstractCredential implements Credential, CredentialMetaData, Serializable {

    /** Serialization version marker. */
    private static final long serialVersionUID = 8196868021183513898L;

    /**
     * @return The credential identifier.
     */
    @Override
    public String toString() {
        return getId();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null) {
            return false;
        }
        if (!(other instanceof Credential)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        final EqualsBuilder builder = new EqualsBuilder();
        builder.append(getId(), ((Credential) other).getId());
        return builder.isEquals();
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder builder = new HashCodeBuilder(11, 41);
        builder.append(getClass().getName());
        builder.append(getId());
        return builder.toHashCode();
    }

    @JsonIgnore
    @Override
    public Class<? extends Credential> getCredentialClass() {
        return this.getClass();
    }
}
