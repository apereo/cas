package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
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

    @Serial
    private static final long serialVersionUID = 5823989148794052951L;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonCreator
    public GroovyRegisteredServiceUsernameProvider(@JsonProperty("groovyScript") final String script) {
        this.groovyScript = script;
    }

    private static String fetchAttributeValue(final RegisteredServiceUsernameProviderContext context,
                                              final String groovyScript) throws Throwable {
        val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager()
            .orElseThrow(() -> new RuntimeException("No groovy script cache manager is available to execute username provider"));
        val script = cacheMgr.resolveScriptableResource(groovyScript,
            context.getRegisteredService().getServiceId(), context.getRegisteredService().getName());
        return Optional.ofNullable(fetchAttributeValueFromScript(script, context.getPrincipal(), context.getService()))
            .map(Object::toString)
            .orElse(null);
    }

    @Override
    public String resolveUsernameInternal(final RegisteredServiceUsernameProviderContext context) throws Throwable {
        if (StringUtils.isNotBlank(groovyScript)) {
            val result = fetchAttributeValue(context, groovyScript);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script", result);
                return result;
            }
        }
        LOGGER.warn("Groovy script [{}] is not valid. CAS will switch to use the default principal identifier [{}]",
            this.groovyScript, context.getPrincipal().getId());
        return context.getPrincipal().getId();
    }

    private static Object fetchAttributeValueFromScript(final ExecutableCompiledScript script,
                                                        final Principal principal, final Service service) throws Throwable {
        val args = CollectionUtils.<String, Object>wrap("attributes", principal.getAttributes(),
            "id", principal.getId(),
            "service", service,
            "logger", LOGGER);
        script.setBinding(args);
        return script.execute(args.values().toArray(), Object.class);
    }

}
