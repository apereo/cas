package org.apereo.cas.authentication.principal;

import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory to create {@link SimplePrincipal} objects.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@EqualsAndHashCode
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


}
