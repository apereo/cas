package org.apereo.cas.services;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public class InMemoryServiceRegistryDaoImpl implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryServiceRegistryDaoImpl.class);
    
    private List<RegisteredService> registeredServices = new ArrayList<>();

    /**
     * Instantiates a new In memory service registry.
     */
    public InMemoryServiceRegistryDaoImpl() {
    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        logWarning();
        return this.registeredServices.remove(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.registeredServices.stream().filter(r -> r.getId() == id).findFirst().orElse(null);
    }

    @Override
    public List<RegisteredService> load() {
        return this.registeredServices;
    }

    @Override
    public RegisteredService save(final RegisteredService registeredService) {
        logWarning();

        if (registeredService.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE) {
            ((AbstractRegisteredService) registeredService).setId(findHighestId() + 1);
        }

        this.registeredServices.remove(registeredService);
        this.registeredServices.add(registeredService);

        return registeredService;
    }

    public void setRegisteredServices(final List registeredServices) {
        this.registeredServices = ObjectUtils.defaultIfNull(registeredServices, new ArrayList<>());
    }

    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId() {
        return this.registeredServices.stream().map(RegisteredService::getId).max(Comparator.naturalOrder()).orElse((long) 0);
    }

    private static void logWarning() {
        LOGGER.warn("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
                + "Changes that are made to service definitions during runtime "
                + "will be LOST upon container restarts.");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public long size() {
        return registeredServices.size();
    }
}
