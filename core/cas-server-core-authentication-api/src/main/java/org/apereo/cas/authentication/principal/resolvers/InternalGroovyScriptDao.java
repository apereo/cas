package org.apereo.cas.authentication.principal.resolvers;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.services.persondir.support.BaseGroovyScriptDaoImpl;
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
@Slf4j
@RequiredArgsConstructor
public class InternalGroovyScriptDao extends BaseGroovyScriptDaoImpl {
    private final ApplicationContext applicationContext;
    private final CasConfigurationProperties casProperties;

    @Override
    public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(final Map<String, List<Object>> attributes) {
        if (attributes.containsKey("username")) {
            val username = attributes.get("username");
            if (!username.isEmpty()) {
                val results = new HashMap<String, List<Object>>();
                val attrs = getAttributesForUser(username.get(0).toString());
                LOGGER.debug("Groovy-based attributes found are [{}]", attrs);
                attrs.forEach((k, v) -> {
                    val values = new ArrayList<Object>(CollectionUtils.toCollection(v));
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
        val finalAttributes = new HashMap<String, Object>();
        casProperties.getAuthn().getAttributeRepository().getGroovy()
            .forEach(groovy -> {
                val args = new Object[] {uid, LOGGER, casProperties, applicationContext};
                val personAttributesMap = ScriptingUtils.executeGroovyScript(groovy.getLocation(), args, Map.class, true);
                finalAttributes.putAll(personAttributesMap);
            });

        return finalAttributes;
    }
}
