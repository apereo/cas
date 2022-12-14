package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;


/**
 * Resolves the username for the service to be the default principal id.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class GroovyRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 5823989148794052951L;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonCreator
    public GroovyRegisteredServiceUsernameProvider(@JsonProperty("groovyScript") final String script) {
        this.groovyScript = script;
    }

    private static String fetchAttributeValue(final Principal principal,
                                              final Service service,
                                              final RegisteredService registeredService,
                                              final String groovyScript) {

        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheMgr -> {
                val script = cacheMgr.resolveScriptableResource(groovyScript, registeredService.getServiceId(), registeredService.getName());
                return fetchAttributeValueFromScript(script, principal, service);
            })
            .map(Object::toString)
            .orElseThrow(() -> new RuntimeException("No groovy script cache manager is available to execute username provider"));
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        if (StringUtils.isNotBlank(this.groovyScript)) {
            val result = fetchAttributeValue(principal, service, registeredService, groovyScript);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script", result);
                return result;
            }
        }
        LOGGER.warn("Groovy script [{}] is not valid. CAS will switch to use the default principal identifier [{}]", this.groovyScript, principal.getId());
        return principal.getId();
    }

    private static Object fetchAttributeValueFromScript(final ExecutableCompiledGroovyScript script,
                                                        final Principal principal, final Service service) {
        val args = CollectionUtils.<String, Object>wrap("attributes", principal.getAttributes(),
            "id", principal.getId(),
            "service", service,
            "logger", LOGGER);
        script.setBinding(args);
        return script.execute(args.values().toArray(), Object.class);
    }

}
