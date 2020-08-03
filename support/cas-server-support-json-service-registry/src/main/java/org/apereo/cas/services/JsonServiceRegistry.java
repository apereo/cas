package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.WatcherService;

import lombok.Getter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

import java.util.Collection;

/**
 * Implementation of {@code ServiceRegistry} that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time. JSON files are
 * expected to be found inside a directory location and this registry will recursively look through
 * the directory structure to find relevant JSON files. Files are expected to have the
 * {@value JsonServiceRegistry#FILE_EXTENSION} extension.
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Getter
public class JsonServiceRegistry extends AbstractResourceBasedServiceRegistry {

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";

    public JsonServiceRegistry(final Resource configDirectory, final WatcherService serviceRegistryConfigWatcher,
                               final ConfigurableApplicationContext applicationContext,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) throws Exception {
        super(configDirectory,
            CollectionUtils.wrapList(new CasAddonsRegisteredServicesJsonSerializer(), new RegisteredServiceJsonSerializer()),
            applicationContext, registeredServiceReplicationStrategy, resourceNamingStrategy,
            serviceRegistryListeners, serviceRegistryConfigWatcher);
    }

    @Override
    protected String[] getExtensions() {
        return new String[]{FILE_EXTENSION};
    }
}
