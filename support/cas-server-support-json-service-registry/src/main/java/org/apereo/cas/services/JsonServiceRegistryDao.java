package org.apereo.cas.services;


import org.apereo.cas.services.util.CasAddonsRegisteredServicesJsonSerializer;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;

import java.nio.file.Path;

/**
 * Implementation of {@code ServiceRegistryDao} that reads services definition from JSON
 * configuration file at the Spring Application Context initialization time. JSON files are
 * expected to be found inside a directory location and this registry will recursively look through
 * the directory structure to find relevant JSON files. Files are expected to have the
 * {@value JsonServiceRegistryDao#FILE_EXTENSION} extension. An example of the JSON file is included here:
 * <pre>
 * {
 * "@class" : "RegexRegisteredService",
 * "id" : 103935657744185,
 * "description" : "This is the application description",
 * "serviceId" : "https://app.school.edu",
 * "name" : "testSaveAttributeReleasePolicyAllowedAttrRulesAndFilter",
 * "theme" : "testtheme",
 * "proxyPolicy" : {
 * "@class" : "RegexMatchingRegisteredServiceProxyPolicy",
 * "pattern" : "https://.+"
 * },
 * "enabled" : true,
 * "ssoEnabled" : false,
 * "evaluationOrder" : 1000,
 * "usernameAttributeProvider" : {
 * "@class" : "DefaultRegisteredServiceUsernameProvider"
 * },
 * "logoutType" : "BACK_CHANNEL",
 * "requiredHandlers" : [ "java.util.HashSet", [ "handler1", "handler2" ] ],
 * "attributeReleasePolicy" : {
 * "@class" : "ReturnAllowedAttributeReleasePolicy",
 * "attributeFilter" : {
 * "@class" : "RegisteredServiceRegexAttributeFilter",
 * "pattern" : "\\w+"
 * },
 * "allowedAttributes" : [ "java.util.ArrayList", [ "uid", "sn", "cn" ] ]
 * }
 * }
 * </pre>
 *
 * @author Dmitriy Kopylenko
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class JsonServiceRegistryDao extends AbstractResourceBasedServiceRegistryDao {

    /**
     * File extension of registered service JSON files.
     */
    private static final String FILE_EXTENSION = "json";

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link DefaultRegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @param enableWatcher   the enable watcher
     * @param eventPublisher  the event publisher
     */
    public JsonServiceRegistryDao(final Path configDirectory, final boolean enableWatcher, final ApplicationEventPublisher eventPublisher) {
        super(configDirectory, new DefaultRegisteredServiceJsonSerializer(), enableWatcher, eventPublisher);
    }

    /**
     * Instantiates a new Json service registry dao.
     * Sets the path to the directory where JSON service registry entries are
     * stored. Uses the {@link DefaultRegisteredServiceJsonSerializer} by default.
     *
     * @param configDirectory the config directory where service registry files can be found.
     * @param enableWatcher   the enable watcher
     * @param eventPublisher  the event publisher
     * @throws Exception the IO exception
     */
    public JsonServiceRegistryDao(final Resource configDirectory,
                                  final boolean enableWatcher,
                                  final ApplicationEventPublisher eventPublisher) throws Exception {
        super(configDirectory,
                CollectionUtils.wrapList(
                        new CasAddonsRegisteredServicesJsonSerializer(),
                        new DefaultRegisteredServiceJsonSerializer()),
                enableWatcher, eventPublisher);
    }

    @Override
    protected String getExtension() {
        return FILE_EXTENSION;
    }
}
