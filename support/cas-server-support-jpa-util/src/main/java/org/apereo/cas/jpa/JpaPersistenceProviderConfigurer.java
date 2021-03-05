package org.apereo.cas.jpa;

import org.springframework.core.Ordered;

/**
 * This is {@link JpaPersistenceProviderConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface JpaPersistenceProviderConfigurer extends Ordered {

    /**
     * Gets managed entities.
     *
     * @param context the context
     */
    void configure(JpaPersistenceProviderContext context);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
