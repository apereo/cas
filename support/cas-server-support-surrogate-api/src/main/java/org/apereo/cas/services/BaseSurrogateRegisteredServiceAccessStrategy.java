package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

/**
 * This is {@link BaseSurrogateRegisteredServiceAccessStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public abstract class BaseSurrogateRegisteredServiceAccessStrategy extends BaseRegisteredServiceAccessStrategy {
    @Serial
    private static final long serialVersionUID = -3975861635454453130L;

    protected boolean isSurrogateAuthenticationSession(final RegisteredServiceAccessStrategyRequest request) {
        return request.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_ENABLED);
    }
}
