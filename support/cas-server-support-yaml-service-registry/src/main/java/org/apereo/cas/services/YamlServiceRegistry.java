package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.Collection;

/**
 * Implementation of {@code ServiceRegistry} that reads services definition from YAML
 * configuration file at the Spring Application Context initialization time. YAML files are
 * expected to be found inside a directory location and this registry will recursively look through
 * the directory structure to find relevant YAML files. Files are expected to have the
 * {@link #getExtensions()} extension. An example of the YAML file is included here:
 * <pre>
 * --- !&lt;org.apereo.cas.services.RegexRegisteredService&gt;
 * serviceId: "testId"
 * name: "YAML"
 * id: 1000
 * description: "description"
 * attributeReleasePolicy: !&lt;org.apereo.cas.services.ReturnAllAttributeReleasePolicy&gt;
 * accessStrategy: !&lt;org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy&gt;
 * enabled: true
 * ssoEnabled: true
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class YamlServiceRegistry extends AbstractResourceBasedServiceRegistry {

    /**
     * File extension of registered service YAML files.
     */
    private static final String[] FILE_EXTENSIONS = new String[] {"yml", "yaml"};

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory                      the config directory where service registry files can be found.
     * @param enableWatcher                        the enable watcher
     * @param eventPublisher                       the event publisher
     * @param registeredServiceReplicationStrategy the registered service replication strategy
     * @param resourceNamingStrategy               the registered service naming strategy
     */
    public YamlServiceRegistry(final Path configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) {
        super(configDirectory, new RegisteredServiceYamlSerializer(), enableWatcher, eventPublisher,
            registeredServiceReplicationStrategy, resourceNamingStrategy, serviceRegistryListeners);
    }

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory                      the config directory where service registry files can be found.
     * @param enableWatcher                        the enable watcher
     * @param eventPublisher                       the event publisher
     * @param registeredServiceReplicationStrategy the registered service replication strategy
     * @param resourceNamingStrategy               the registered service naming strategy
     * @throws Exception the IO exception
     */
    public YamlServiceRegistry(final Resource configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) throws Exception {
        super(configDirectory, CollectionUtils.wrapList(new RegisteredServiceYamlSerializer()), enableWatcher,
            eventPublisher, registeredServiceReplicationStrategy, resourceNamingStrategy, serviceRegistryListeners);
    }

    @Override
    protected String[] getExtensions() {
        return FILE_EXTENSIONS;
    }
}
