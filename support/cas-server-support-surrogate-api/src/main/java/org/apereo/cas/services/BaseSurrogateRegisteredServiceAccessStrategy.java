package org.apereo.cas.services;

import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

import java.io.Serial;

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
