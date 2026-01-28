package org.apereo.cas.persondir.groovy;

import module java.base;
import org.apereo.cas.authentication.attribute.SimpleUsernameAttributeProvider;
import org.apereo.cas.authentication.principal.attribute.PersonAttributes;
import org.apereo.cas.authentication.principal.attribute.UsernameAttributeProvider;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.GroovyPrincipalAttributesProperties;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

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
            FunctionUtils.doAndHandle(_ -> {
                val allAttributes = new HashMap<>(attributes);
                if (resultPeople != null && !resultPeople.isEmpty()) {
                    allAttributes.put("people", new ArrayList<>(resultPeople));
                }
                val scriptLocation = groovyPrincipalAttributesProperties.getLocation().getURI().toString();
                val finalAttributes = ApplicationContextProvider.getScriptResourceCacheManager()
                    .map(cacheManager -> {
                        val script = cacheManager.resolveScriptableResource(scriptLocation);
                        val args = new Object[]{username, allAttributes, LOGGER, casProperties, applicationContext};
                        return Objects.requireNonNull(script).execute(args, Map.class, true);
                    })
                    .orElseGet(Map::of);

                LOGGER.debug("Groovy-based attributes found are [{}]", finalAttributes);
                finalAttributes.forEach((key, v) -> {
                    val values = new ArrayList<>(CollectionUtils.toCollection(v));
                    LOGGER.trace("Adding Groovy-based attribute [{}] with value(s) [{}]", key, values);
                    results.put(key.toString(), values);
                });
            });
        }
        return results;
    }
}
