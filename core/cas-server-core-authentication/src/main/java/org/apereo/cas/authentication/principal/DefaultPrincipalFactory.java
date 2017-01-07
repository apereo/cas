package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create {@link SimplePrincipal} objects.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class DefaultPrincipalFactory implements PrincipalFactory {
    private static final long serialVersionUID = -3999695695604948495L;

    @Override
    public Principal createPrincipal(final String id) {
        return new SimplePrincipal(id, new HashMap<>());
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
        return obj.getClass() == getClass();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 33).toHashCode();
    }
}
