package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.support.events.service.CasRegisteredServiceLoadedEvent;

import java.util.List;

/**
 * This is {@link DynamoDbServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class DynamoDbServiceRegistryDao extends AbstractServiceRegistryDao {
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
    public List<RegisteredService> load() {
        final List<RegisteredService> svc = dbTableService.getAll();
        svc.stream().forEach(s -> publishEvent(new CasRegisteredServiceLoadedEvent(this, s)));
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
