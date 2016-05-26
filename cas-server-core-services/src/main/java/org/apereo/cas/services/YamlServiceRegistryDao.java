package org.apereo.cas.services;


import org.apereo.cas.util.services.RegisteredServiceYamlSerializer;
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
     */
    public YamlServiceRegistryDao(final Path configDirectory) {
        super(configDirectory, new RegisteredServiceYamlSerializer());
    }

    /**
     * Instantiates a new YAML service registry dao.
     * Sets the path to the directory where YAML service registry entries are
     * stored. Uses the {@link RegisteredServiceYamlSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @throws Exception the IO exception
     */
    @Autowired
    public YamlServiceRegistryDao(@Value("${service.registry.config.location:classpath:services}")
                                  final Resource configDirectory) throws Exception {
        super(configDirectory, new RegisteredServiceYamlSerializer());
    }


    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
