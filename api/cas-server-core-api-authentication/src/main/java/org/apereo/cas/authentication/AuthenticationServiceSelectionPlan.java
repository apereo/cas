package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link AuthenticationServiceSelectionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationServiceSelectionPlan {
    /**
     * Bean name.
     */
    String BEAN_NAME = "authenticationServiceSelectionPlan";

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
     * @throws Throwable the throwable
     */
    @Nullable Service resolveService(@Nullable Service service) throws Throwable;

    /**
     * Resolve service t.
     *
     * @param <T>     the type parameter
     * @param service the service
     * @param clazz   the clazz
     * @return the t
     * @throws Throwable the throwable
     */
    @Nullable <T extends Service> T resolveService(Service service, Class<T> clazz) throws Throwable;
}
