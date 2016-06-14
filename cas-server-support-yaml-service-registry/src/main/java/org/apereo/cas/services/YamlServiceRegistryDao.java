package org.apereo.cas.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Implementation of {@code ServiceRegistryDao} that reads services definition from YAML
 * configuration file at the Spring Application Context initialization time. YAML files are
 * expected to be found inside a directory location and this DAO will recursively look through
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
@RefreshScope
@Component("yamlServiceRegistryDao")
public class YamlServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(YamlServiceRegistryDao.class);

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
     */
    public YamlServiceRegistryDao(final Path configDirectory, final boolean enableWatcher) {
        super(configDirectory, new RegisteredServiceYamlSerializer(), enableWatcher);
    }

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @param enableWatcher   the enable watcher
     * @throws Exception the IO exception
     */
    @Autowired
    public YamlServiceRegistryDao(@Value("${service.registry.config.location:classpath:services}")
                                  final Resource configDirectory,
                                  @Value("${service.registry.watcher.enabled:true}")
                                  final boolean enableWatcher) throws Exception {
        super(configDirectory, new RegisteredServiceYamlSerializer(), enableWatcher);
    }


    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
