package org.apereo.cas.authentication.principal;

import module java.base;
import lombok.EqualsAndHashCode;
import org.jspecify.annotations.Nullable;

/**
 * Factory to create {@link SimplePrincipal} objects.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@EqualsAndHashCode
public class DefaultPrincipalFactory implements PrincipalFactory {
    @Serial
    private static final long serialVersionUID = -3999695695604948495L;

    @Override
    public @Nullable Principal createPrincipal(final String id, final Map<String, List<Object>> attributes) throws Throwable {
        return new SimplePrincipal(id, attributes);
    }
}
