package org.apereo.cas.services;

import org.springframework.beans.factory.InitializingBean;

/**
 * This is {@link ServiceRegistryInitializer}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface ServiceRegistryInitializer extends InitializingBean {
    /**
     * Initialize.
     */
    void initialize();

    @Override
    default void afterPropertiesSet() throws Exception {
        initialize();
    }
}
