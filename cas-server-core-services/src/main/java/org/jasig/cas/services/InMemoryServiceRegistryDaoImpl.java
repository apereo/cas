package org.jasig.cas.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Default In Memory Service Registry Dao for test/demonstration purposes.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("inMemoryServiceRegistryDao")
public final class InMemoryServiceRegistryDaoImpl implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryServiceRegistryDaoImpl.class);

    @NotNull
    private List<RegisteredService> registeredServices = new ArrayList<>();

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Instantiates a new In memory service registry.
     */
    public InMemoryServiceRegistryDaoImpl() {
    }


    /**
     * After properties set.
     */
    @PostConstruct
    public void afterPropertiesSet() {
        final String[] aliases =
            this.applicationContext.getAutowireCapableBeanFactory().getAliases("inMemoryServiceRegistryDao");
        if (aliases.length > 0) {
            LOGGER.debug("{} is used as the active service registry dao", this.getClass().getSimpleName());

            try {
                final List<RegisteredService> list = (List<RegisteredService>)
                    this.applicationContext.getBean("inMemoryRegisteredServices", List.class);
                if (list != null) {
                    LOGGER.debug("Loaded {} services from the application context for {}",
                        list.size(),
                        this.getClass().getSimpleName());
                    this.registeredServices = list;
                }
            } catch (final Exception e) {
                LOGGER.debug("No registered services are defined for {}", this.getClass().getSimpleName());
            }
        }

    }

    @Override
    public boolean delete(final RegisteredService registeredService) {
        logWarning();
        return this.registeredServices.remove(registeredService);
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() == id) {
                return r;
            }
        }

        return null;
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
        this.registeredServices = registeredServices;
    }

    /**
     * This isn't super-fast but we don't expect thousands of services.
     *
     * @return the highest service id in the list of registered services
     */
    private long findHighestId() {
        long id = 0;

        for (final RegisteredService r : this.registeredServices) {
            if (r.getId() > id) {
                id = r.getId();
            }
        }

        return id;
    }

    private void logWarning() {
        LOGGER.debug("Runtime memory is used as the persistence storage for retrieving and persisting service definitions. "
            + "Changes that are made to service definitions during runtime "
            + "will be LOST upon container restarts.");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
