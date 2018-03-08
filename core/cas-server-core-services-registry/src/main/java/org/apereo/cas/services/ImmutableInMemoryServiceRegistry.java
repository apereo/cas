package org.apereo.cas.services;

import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This is {@link ImmutableInMemoryServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor
public class ImmutableInMemoryServiceRegistry extends InMemoryServiceRegistry implements ImmutableServiceRegistry {
    public ImmutableInMemoryServiceRegistry(final List<RegisteredService> registeredServices) {
        super(registeredServices);
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return registeredService;
    }
}
