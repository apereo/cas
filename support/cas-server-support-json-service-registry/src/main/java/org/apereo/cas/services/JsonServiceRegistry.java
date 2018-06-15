package org.apereo.cas.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;

import java.nio.file.Path;

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
@Slf4j
@Getter
public class JsonServiceRegistry extends AbstractResourceBasedServiceRegistry {

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link DefaultRegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory                      the config directory where service registry files can be found.
     * @param enableWatcher                        the enable watcher
     * @param eventPublisher                       the event publisher
     * @param registeredServiceReplicationStrategy the registered service replication strategy
     * @param resourceNamingStrategy               the registered service namimg strategy
     */
    public JsonServiceRegistry(final Path configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy) {
        super(configDirectory, new DefaultRegisteredServiceJsonSerializer(), enableWatcher,
            eventPublisher, registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link DefaultRegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory                      the config directory where service registry files can be found.
     * @param enableWatcher                        the enable watcher
     * @param eventPublisher                       the event publisher
     * @param registeredServiceReplicationStrategy the registered service replication strategy
     * @param resourceNamingStrategy               the registered service naming strategy
     * @throws Exception the IO exception
     */
    public JsonServiceRegistry(final Resource configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy) throws Exception {
        super(configDirectory, CollectionUtils.wrapList(new CasAddonsRegisteredServicesJsonSerializer(),
            new DefaultRegisteredServiceJsonSerializer()), enableWatcher, eventPublisher,
            registeredServiceReplicationStrategy, resourceNamingStrategy);
    }

    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
