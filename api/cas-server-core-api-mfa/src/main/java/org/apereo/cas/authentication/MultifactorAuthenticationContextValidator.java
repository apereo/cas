package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.services.RegisteredService;

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
     * Default impl bean name.
     */
    String BEAN_NAME = "authenticationContextValidator";

    /**
     * Validate the authentication context.
     *
     * @param authentication   the authentication
     * @param requestedContext the requested context
     * @param service          the service
     * @return the result indicates whether context is satisfied, and if so, by which provider.
     */
    MultifactorAuthenticationContextValidationResult validate(Authentication authentication,
                                                              String requestedContext,
                                                              Optional<RegisteredService> service);
}
