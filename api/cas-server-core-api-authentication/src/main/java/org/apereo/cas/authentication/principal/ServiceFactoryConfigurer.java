package org.apereo.cas.authentication.principal;

import module java.base;

/**
 * This is {@link ServiceFactoryConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface ServiceFactoryConfigurer {

    /**
     * Build service factories collection.
     *
     * @return the collection
     */
    Collection<ServiceFactory<? extends WebApplicationService>> buildServiceFactories();
}
