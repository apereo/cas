package org.apereo.cas.authentication;

import org.apereo.cas.services.RegisteredService;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

/**
 * This is {@link MultifactorAuthenticationContextValidator}, which is responsible for evaluating an authentication
 * object to see whether it satisfied a requested authentication context.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationContextValidator {

    /**
     * Validate the authentication context.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @param service          the service
     * @return the resulting pair indicates whether context is satisfied, and if so, by which provider.
     */
    Pair<Boolean, Optional<MultifactorAuthenticationProvider>> validate(Authentication authentication,
                                                                        String requestedContext,
                                                                        RegisteredService service);
}
