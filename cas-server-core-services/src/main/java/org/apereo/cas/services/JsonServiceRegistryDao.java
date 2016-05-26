package org.apereo.cas.services;


import org.apereo.cas.util.services.RegisteredServiceJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Implementation of {@code ServiceRegistryDao} that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time. JSON files are
 * expected to be found inside a directory location and this DAO will recursively look through
 * the directory structure to find relevant JSON files. Files are expected to have the
 * {@value JsonServiceRegistryDao#FILE_EXTENSION} extension. An example of the JSON file is included here:
 * <pre>
 {
     "@class" : "RegexRegisteredService",
     "id" : 103935657744185,
     "description" : "This is the application description",
     "serviceId" : "https://app.school.edu",
     "name" : "testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter",
     "theme" : "testtheme",
     "proxyPolicy" : {
        "@class" : "RegexMatchingRegisteredServiceProxyPolicy",
        "pattern" : "https://.+"
     },
     "enabled" : true,
     "ssoEnabled" : false,
     "evaluationOrder" : 1000,
     "usernameAttributeProvider" : {
        "@class" : "DefaultRegisteredServiceUsernameProvider"
     },
     "logoutType" : "BACK_CHANNEL",
     "requiredHandlers" : [ "java.util.HashSet", [ "handler1", "handler2" ] ],
     "attributeReleasePolicy" : {
        "@class" : "ReturnAllowedAttributeReleasePolicy",
        "attributeFilter" : {
            "@class" : "RegisteredServiceRegexAttributeFilter",
            "pattern" : "\\w+"
        },
        "allowedAttributes" : [ "java.util.ArrayList", [ "uid", "sn", "cn" ] ]
     }
 }
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@RefreshScope
@Component("jsonServiceRegistryDao")
public class JsonServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonServiceRegistryDao.class);

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";
    
    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link RegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     */
    public JsonServiceRegistryDao(final Path configDirectory) {
        super(configDirectory, new RegisteredServiceJsonSerializer());
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link RegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @throws Exception the IO exception
     */
    @Autowired
    public JsonServiceRegistryDao(@Value("${service.registry.config.location:classpath:services}")
                                  final Resource configDirectory) throws Exception {
        super(configDirectory, new RegisteredServiceJsonSerializer());
    }


    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
