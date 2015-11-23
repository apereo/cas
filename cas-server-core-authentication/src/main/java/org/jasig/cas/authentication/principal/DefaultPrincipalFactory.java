package org.jasig.cas.authentication.principal;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Factory to create {@link org.jasig.cas.authentication.principal.SimplePrincipal} objects.
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Component("defaultPrincipalFactory")
@Scope("prototype")
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

}
