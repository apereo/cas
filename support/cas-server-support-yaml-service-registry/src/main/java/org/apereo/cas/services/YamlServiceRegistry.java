package org.apereo.cas.services;

import org.apereo.cas.services.replication.RegisteredServiceReplicationStrategy;
import org.apereo.cas.services.resource.AbstractResourceBasedServiceRegistry;
import org.apereo.cas.services.resource.RegisteredServiceResourceNamingStrategy;
import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.io.WatcherService;

import lombok.Getter;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;

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
    private static final String[] FILE_EXTENSIONS = new String[]{"yml", "yaml"};

    public YamlServiceRegistry(final Resource configDirectory,
                               final WatcherService serviceRegistryConfigWatcher,
                               final ConfigurableApplicationContext applicationContext,
                               final RegisteredServiceReplicationStrategy registeredServiceReplicationStrategy,
                               final RegisteredServiceResourceNamingStrategy resourceNamingStrategy,
                               final Collection<ServiceRegistryListener> serviceRegistryListeners) throws Exception {
        super(configDirectory,
            CollectionUtils.wrapList(new RegisteredServiceYamlSerializer()),
            applicationContext, registeredServiceReplicationStrategy,
            resourceNamingStrategy, serviceRegistryListeners, serviceRegistryConfigWatcher);
    }

    @Override
    protected String[] getExtensions() {
        return FILE_EXTENSIONS;
    }
}
