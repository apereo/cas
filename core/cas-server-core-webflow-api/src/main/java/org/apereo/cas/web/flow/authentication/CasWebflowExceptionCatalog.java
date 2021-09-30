package org.apereo.cas.web.flow.authentication;

import java.util.Collection;
import java.util.Set;

/**
 * This is {@link CasWebflowExceptionCatalog}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface CasWebflowExceptionCatalog {
    /**
     * Register exception.
     *
     * @param throwable the throwable
     */
    void registerException(Class<? extends Throwable> throwable);

    /**
     * Register exception.
     *
     * @param throwable the throwable
     */
    void registerExceptions(Collection<Class<? extends Throwable>> throwable);

    /**
     * Gets registered exceptions.
     *
     * @return the registered exceptions
     */
    Set<Class<? extends Throwable>> getRegisteredExceptions();
}
