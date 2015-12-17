package org.jasig.cas.authentication.principal;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Factory to create {@link org.jasig.cas.authentication.principal.SimplePrincipal} objects.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Component("defaultPrincipalFactory")
public final class DefaultPrincipalFactory implements PrincipalFactory {
    private static final long serialVersionUID = -3999695695604948495L;

    @Override
    public Principal createPrincipal(final String id) {
        return new SimplePrincipal(id, Collections.EMPTY_MAP);
    }

    @Override
    public Principal createPrincipal(final String id, final Map<String, Object> attributes) {
        return new SimplePrincipal(id, attributes);
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
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 33).toHashCode();
    }
}
