package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;

import java.io.IOException;
import java.util.Map;

/**
 * Default implementation of {@link PrincipalAttributesRepository}
 * that just returns the attributes as it receives them.
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = -4535358847021241725L;

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        logger.debug("Using {}, no caching takes place for {} to add attributes.", id,
                this.getClass().getSimpleName());
    }

    @Override
    protected Map<String, Object> getPrincipalAttributes(final Principal p) {
        logger.debug("{} will return the collection of attributes directly associated with the principal object",
                this.getClass().getSimpleName());
        return p.getAttributes();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .toString();
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
        final DefaultPrincipalAttributesRepository rhs = (DefaultPrincipalAttributesRepository) obj;
        return new EqualsBuilder()
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133)
                .toHashCode();
    }

    @Override
    public void close() throws IOException {}
}
