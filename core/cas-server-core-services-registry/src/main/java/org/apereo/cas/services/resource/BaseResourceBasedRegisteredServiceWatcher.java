package org.apereo.cas.services.resource;

import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.function.Consumer;

/**
 * This is {@link BaseResourceBasedRegisteredServiceWatcher}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseResourceBasedRegisteredServiceWatcher implements Consumer<File> {
    /**
     * Consumer to log warnings for duplicate service defns.
     */
    static final Consumer<RegisteredService> LOG_SERVICE_DUPLICATE =
        service -> LOGGER.warn("Found a service definition [{}] with a duplicate id [{}]. "
            + "This will overwrite previous service definitions and is likely a configuration problem. "
            + "Make sure all services have a unique id and try again.", service.getServiceId(), service.getId());

    /**
     * Service registry instance.
     */
    protected final AbstractResourceBasedServiceRegistry serviceRegistryDao;

    @Override
    public void accept(final File file) {
    }
}
