package org.apereo.cas.authentication.principal;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.cache.AbstractPrincipalAttributesRepository;

import java.util.Map;

/**
 * Default implementation of {@link PrincipalAttributesRepository}
 * that just returns the attributes as it receives them.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Slf4j
@ToString
@EqualsAndHashCode(callSuper = true)
public class DefaultPrincipalAttributesRepository extends AbstractPrincipalAttributesRepository {

    private static final long serialVersionUID = -4535358847021241725L;

    @Override
    protected void addPrincipalAttributes(final String id, final Map<String, Object> attributes) {
        LOGGER.debug("Using [{}], no caching takes place for [{}] to add attributes.", id, this.getClass().getSimpleName());
    }

    @Override
    protected Map<String, Object> getPrincipalAttributes(final Principal p) {
        final var attributes = p.getAttributes();
        LOGGER.debug("[{}] will return the collection of attributes directly associated with the principal object which are [{}]",
            this.getClass().getSimpleName(), attributes);
        return attributes;
    }

    @Override
    public void close() {
    }
}
