package org.apereo.cas.services;

import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

/**
 * This is {@link ImmutableInMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class ImmutableInMemoryServiceRegistry extends InMemoryServiceRegistry implements ImmutableServiceRegistry {
    public ImmutableInMemoryServiceRegistry(final List<RegisteredService> registeredServices,
                                            final ApplicationEventPublisher eventPublisher) {
        super(eventPublisher, registeredServices);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return registeredService;
    }
}
