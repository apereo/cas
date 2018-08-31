package org.apereo.cas.services;

import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;

/**
 * This is {@link DynamoDbServiceRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DynamoDbServiceRegistry extends AbstractServiceRegistry {
    private final DynamoDbServiceRegistryFacilitator dbTableService;

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        dbTableService.put(registeredService);
        return registeredService;
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        return dbTableService.delete(registeredService);
    }

    @Override
    public Collection<RegisteredService> load() {
        val svc = dbTableService.getAll();
        svc.forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
        return svc;
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return dbTableService.get(id);
    }

    @Override
    public RegisteredService findServiceById(final String id) {
        return dbTableService.get(id);
    }

    @Override
    public long size() {
        return dbTableService.count();
    }
}
