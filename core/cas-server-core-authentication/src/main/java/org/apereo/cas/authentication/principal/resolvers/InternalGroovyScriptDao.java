package org.apereo.cas.authentication.principal.resolvers;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.commons.collections.map.HashedMap;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.services.persondir.support.BaseGroovyScriptDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link InternalGroovyScriptDao}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InternalGroovyScriptDao extends BaseGroovyScriptDaoImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(InternalGroovyScriptDao.class);

    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    /**
     * Instantiates a new Internal groovy script dao.
     *
     * @param applicationContext the application context
     * @param casProperties      the cas properties
     */
    public InternalGroovyScriptDao(final ApplicationContext applicationContext, final CasConfigurationProperties casProperties) {
        this.applicationContext = applicationContext;
        this.casProperties = casProperties;
    }

    @Override
    public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(final Map<String, List<Object>> attributes) {
        if (attributes.containsKey("username")) {
            final List<Object> a = attributes.get("username");
            if (!a.isEmpty()) {
                final Map<String, List<Object>> results = new HashMap<>();
                final Map<String, Object> attrs = getAttributesForUser(a.get(0).toString());
                LOGGER.debug("Groovy-based attributes found are [{}]", attrs);
                attrs.forEach((k, v) -> {
                    final List<Object> values = new ArrayList<>(CollectionUtils.toCollection(v));
                    LOGGER.debug("Adding Groovy-based attribute [{}] with value(s) [{}]", k, values);
                    results.put(k, values);
                });
                return results;
            }
        }
        return new HashMap<>();
    }

    @Override
    public Map<String, Object> getAttributesForUser(final String uid) {
        final Map<String, Object> finalAttributes = new HashedMap();
        casProperties.getAuthn().getAttributeRepository().getGroovy().forEach(groovy -> {
            final ClassLoader parent = getClass().getClassLoader();
            try (GroovyClassLoader loader = new GroovyClassLoader(parent)) {
                if (groovy.getConfig().getLocation() != null) {
                    final File groovyFile = groovy.getConfig().getLocation().getFile();
                    if (groovyFile.exists()) {
                        final Class<?> groovyClass = loader.parseClass(groovyFile);
                        LOGGER.debug("Loaded groovy class [{}] from script [{}]", groovyClass.getSimpleName(), groovyFile.getCanonicalPath());
                        final GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
                        LOGGER.debug("Created groovy object instance from class [{}]", groovyFile.getCanonicalPath());
                        final Object[] args = {uid, LOGGER, casProperties, applicationContext};
                        LOGGER.debug("Executing groovy script's run method, with parameters [{}]", args);
                        final Map<String, Object> personAttributesMap = (Map<String, Object>) groovyObject.invokeMethod("run", args);
                        LOGGER.debug("Creating person attributes with the username [{}] and attributes [{}]", uid, personAttributesMap);
                        finalAttributes.putAll(personAttributesMap);
                    }
                }
            } catch (final Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

        return finalAttributes;
    }
}
