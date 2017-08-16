package org.apereo.cas.ticket.support;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.ticket.ExpirationPolicy;

import java.util.UUID;

/**
 * This is an {@link AbstractCasExpirationPolicy}
 * that serves as the root parent for all CAS expiration policies
 * and exposes a few internal helper methods to children can access
 * to objects like the request, etc.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public abstract class AbstractCasExpirationPolicy implements ExpirationPolicy {

    private static final long serialVersionUID = 8042104336580063690L;

    private String name;

    public AbstractCasExpirationPolicy() {
        this.name = this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString();
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
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
        final AbstractCasExpirationPolicy rhs = (AbstractCasExpirationPolicy) obj;
        return new EqualsBuilder()
                .append(this.name, rhs.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .toHashCode();
    }
}
