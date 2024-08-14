package org.apereo.cas.services;

import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link ImmutableInMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ImmutableInMemoryServiceRegistry extends InMemoryServiceRegistry implements ImmutableServiceRegistry {
    public ImmutableInMemoryServiceRegistry(final List<RegisteredService> registeredServices,
                                            final ConfigurableApplicationContext applicationContext,
                                            final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(applicationContext, registeredServices, serviceRegistryListeners);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return registeredService;
    }
}
