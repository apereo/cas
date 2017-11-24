package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link BaseResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public abstract class BaseResourceBasedRegisteredServiceWatcher implements Consumer<File> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseResourceBasedRegisteredServiceWatcher.class);

    /**
     * Consumer to log warnings for duplicate service defns.
     */
    public static final Consumer<RegisteredService> LOG_SERVICE_DUPLICATE =
        service -> LOGGER.warn("Found a service definition [{}] with a duplicate id [{}]. "
            + "This will overwrite previous service definitions and is likely a configuration problem. "
            + "Make sure all services have a unique id and try again.", service.getServiceId(), service.getId());


    /**
     * Service registry instance.
     */
    protected AbstractResourceBasedServiceRegistryDao serviceRegistryDao;

    public BaseResourceBasedRegisteredServiceWatcher(final AbstractResourceBasedServiceRegistryDao serviceRegistryDao) {
        this.serviceRegistryDao = serviceRegistryDao;
    }

    @Override
    public void accept(final File file) {
    }
}
