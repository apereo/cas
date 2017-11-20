package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.services.persondir.support.BaseGroovyScriptDaoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

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
            final List<Object> username = attributes.get("username");
            if (!username.isEmpty()) {
                final Map<String, List<Object>> results = new HashMap<>();
                final Map<String, Object> attrs = getAttributesForUser(username.get(0).toString());
                LOGGER.debug("Groovy-based attributes found are [{}]", attrs);
                attrs.forEach((k, v) -> {
                    final List<Object> values = new ArrayList<>(CollectionUtils.toCollection(v));
                    LOGGER.debug("Adding Groovy-based attribute [{}] with value(s) [{}]", k, values);
                    results.put(k, values);
                });
                return results;
            }
        }
        return new HashMap<>(0);
    }

    @Override
    public Map<String, Object> getAttributesForUser(final String uid) {
        final Map<String, Object> finalAttributes = new HashMap<>();
        casProperties.getAuthn().getAttributeRepository().getGroovy().forEach(groovy -> {
            final Object[] args = {uid, LOGGER, casProperties, applicationContext};
            final Map<String, Object> personAttributesMap =
                    ScriptingUtils.executeGroovyScript(groovy.getLocation(), args, Map.class);
            finalAttributes.putAll(personAttributesMap);
        });

        return finalAttributes;
    }
}
