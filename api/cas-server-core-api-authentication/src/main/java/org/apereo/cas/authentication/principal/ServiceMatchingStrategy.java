package org.apereo.cas.authentication.principal;
import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * This is {@link ServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@FunctionalInterface
public interface ServiceMatchingStrategy {
    /**
     * Always matches service matching strategy.
     *
     * @return the service matching strategy
     */
    static ServiceMatchingStrategy alwaysMatches() {
        return (service, matchService) -> true;
    }

    /**
     * Never matches service matching strategy.
     *
     * @return the service matching strategy
     */
    static ServiceMatchingStrategy neverMatches() {
        return (service, matchService) -> false;
    }

    /**
     * Determine whether a service matches another
     * primarily used for validation events.
     *
     * @param service        the service
     * @param serviceToMatch the match service
     * @return true /false
     */
    boolean matches(@Nullable Service service, @Nullable Service serviceToMatch);
}
