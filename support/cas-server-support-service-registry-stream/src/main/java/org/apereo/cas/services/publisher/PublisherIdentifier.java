package org.apereo.cas.services.publisher;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.UUID;

/**
 * This is {@link PublisherIdentifier}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class PublisherIdentifier {
    private String id;

    public PublisherIdentifier(final String id) {
        this.id = id;
    }

    public PublisherIdentifier() {
        this(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
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
        final PublisherIdentifier rhs = (PublisherIdentifier) obj;
        return new EqualsBuilder()
                .append(this.id, rhs.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .toHashCode();
    }
}
