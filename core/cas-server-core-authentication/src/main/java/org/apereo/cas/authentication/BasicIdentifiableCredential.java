package org.apereo.cas.authentication;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * This is {@link BasicIdentifiableCredential}, a simple credential implementation
 * that is only recognized by its id. The id generally represents an authentication token
 * encrypted in some fashion.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class BasicIdentifiableCredential implements Credential, Serializable {

    private static final long serialVersionUID = -700605020472810939L;

    private String id;

    /**
     * Instantiates a new Basic identifiable credential.
     *
     * @param id the id
     */
    public BasicIdentifiableCredential(final String id) {
        this.id = id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return this.id;
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
        final BasicIdentifiableCredential rhs = (BasicIdentifiableCredential) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.id)
                .toHashCode();
    }
}

