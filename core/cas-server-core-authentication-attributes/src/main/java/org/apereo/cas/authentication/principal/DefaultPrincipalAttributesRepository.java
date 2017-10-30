package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Default implementation of {@link PrincipalAttributesRepository}
 * that just returns the attributes as it receives them.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {
    private static final long serialVersionUID = -4535358847021241725L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPrincipalAttributesRepository.class);

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        LOGGER.debug("Using [{}], no caching takes place for [{}] to add attributes.", id, this.getClass().getSimpleName());
    }

    @Override
    protected Map<String, Object> getPrincipalAttributes(final Principal p) {
        LOGGER.debug("[{}] will return the collection of attributes directly associated with the principal object which are [{}]",
                this.getClass().getSimpleName(), p.getAttributes());
        return p.getAttributes();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
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
        return new EqualsBuilder().isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 133).toHashCode();
    }

    @Override
    public void close() {
    }
}
