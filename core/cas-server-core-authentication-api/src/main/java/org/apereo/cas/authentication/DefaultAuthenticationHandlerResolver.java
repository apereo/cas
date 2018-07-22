package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * This is {@link DefaultAuthenticationHandlerResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultAuthenticationHandlerResolver implements AuthenticationHandlerResolver {
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
