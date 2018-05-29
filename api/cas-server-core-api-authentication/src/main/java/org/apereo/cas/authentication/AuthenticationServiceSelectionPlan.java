package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link AuthenticationServiceSelectionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationServiceSelectionPlan {
    /**
     * Register strategy handler.
     *
     * @param strategy the strategy
     */
    void registerStrategy(AuthenticationServiceSelectionStrategy strategy);

    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    Service resolveService(Service service);

    /**
     * Resolve service t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param clazz   the clazz
     * @return the t
     */
    <T extends Service> T resolveService(Service service, Class<T> clazz);
}
