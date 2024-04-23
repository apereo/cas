package org.apereo.cas.persondir.groovy;

import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyPrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link InternalGroovyScriptDao}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class InternalGroovyScriptDao implements PersonAttributeScriptDao {
    private final UsernameAttributeProvider usernameAttributeProvider = new SimpleUsernameAttributeProvider("username");

    private final ApplicationContext applicationContext;

    private final CasConfigurationProperties casProperties;

    private final GroovyPrincipalAttributesProperties groovyPrincipalAttributesProperties;

    @Override
    public Map<String, List<Object>> getPersonAttributesFromMultivaluedAttributes(final Map<String, List<Object>> attributes,
                                                                                  final Set<PersonAttributes> resultPeople) {
        val username = usernameAttributeProvider.getUsernameFromQuery(attributes);
        val results = new HashMap<String, List<Object>>();
        if (StringUtils.isNotBlank(username)) {
            val allAttributes = new HashMap<>(attributes);
            if (resultPeople != null && !resultPeople.isEmpty()) {
                allAttributes.put("people", new ArrayList<>(resultPeople));
            }
            val args = new Object[]{username, allAttributes, LOGGER, casProperties, applicationContext};
            val finalAttributes = (Map<String, ?>) ScriptingUtils.executeGroovyScript(
                groovyPrincipalAttributesProperties.getLocation(), args, Map.class, true);
            LOGGER.debug("Groovy-based attributes found are [{}]", finalAttributes);

            finalAttributes.forEach((key, v) -> {
                val values = new ArrayList<Object>(CollectionUtils.toCollection(v));
                LOGGER.trace("Adding Groovy-based attribute [{}] with value(s) [{}]", key, values);
                results.put(key, values);
            });
        }
        return results;
    }
}
