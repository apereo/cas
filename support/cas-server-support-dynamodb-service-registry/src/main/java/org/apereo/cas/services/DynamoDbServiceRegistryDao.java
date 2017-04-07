package org.apereo.cas.services;

import java.util.List;

/**
 * This is {@link DynamoDbServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbServiceRegistryDao implements ServiceRegistryDao {
    private final DynamoDbServiceRegistryFacilitator dbTableService;

    public DynamoDbServiceRegistryDao(final DynamoDbServiceRegistryFacilitator dbTableService) {
        this.dbTableService = dbTableService;
    }

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
        return dbTableService.getAll();
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
