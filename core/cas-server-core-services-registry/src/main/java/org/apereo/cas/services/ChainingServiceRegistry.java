package org.apereo.cas.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link ChainingServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class ChainingServiceRegistry extends AbstractServiceRegistry {
    private final Collection<ServiceRegistryDao> serviceRegistries;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        return null;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return false;
    }

    @Override
    public List<RegisteredService> load() {
        return null;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return null;
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return null;
    }

    @Override
    public RegisteredService findServiceByExactServiceId(final String id) {
        return null;
    }

    @Override
    public RegisteredService findServiceByExactServiceName(final String name) {
        return null;
    }

    @Override
    public long size() {
        return 0;
    }
}
