package org.apereo.cas.services;


import org.apereo.cas.services.util.RegisteredServiceYamlSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;

import java.nio.file.Path;

/**
 * Implementation of {@code ServiceRegistryDao} that reads services definition from YAML
 * configuration file at the Spring Application Context initialization time. YAML files are
 * expected to be found inside a directory location and this registry will recursively look through
 * the directory structure to find relevant YAML files. Files are expected to have the
 * {@value YamlServiceRegistryDao#FILE_EXTENSION} extension. An example of the YAML file is included here:
 * <pre>
--- !&lt;org.apereo.cas.services.RegexRegisteredService&gt;
serviceId: "testId"
name: "YAML"
id: 1000
description: "description"
attributeReleasePolicy: !&lt;org.apereo.cas.services.ReturnAllAttributeReleasePolicy&gt;
accessStrategy: !&lt;org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy&gt;
  enabled: true
  ssoEnabled: true
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class YamlServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {

    /**
     * File extension of registered service YAML files.
     */
    private static final String FILE_EXTENSION = "yml";

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @param enableWatcher   the enable watcher
     * @param eventPublisher  the event publisher
     */
    public YamlServiceRegistryDao(final Path configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher) {
        super(configDirectory, new RegisteredServiceYamlSerializer(), enableWatcher, eventPublisher);
    }

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @param enableWatcher   the enable watcher
     * @param eventPublisher  the event publisher
     * @throws Exception the IO exception
     */
    public YamlServiceRegistryDao(final Resource configDirectory,
                                  final boolean enableWatcher,
                                  final ApplicationEventPublisher eventPublisher) throws Exception {
        super(configDirectory, CollectionUtils.wrapList(new RegisteredServiceYamlSerializer()), enableWatcher, eventPublisher);
    }


    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
