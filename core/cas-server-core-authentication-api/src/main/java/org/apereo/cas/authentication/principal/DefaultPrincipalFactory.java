package org.apereo.cas.authentication.principal;

import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Factory to create {@link SimplePrincipal} objects.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@EqualsAndHashCode
public class DefaultPrincipalFactory implements PrincipalFactory {
    private static final long serialVersionUID = -3999695695604948495L;

    @Override
    public Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) {
        return new SimplePrincipal(id, attributes);
    }
}
