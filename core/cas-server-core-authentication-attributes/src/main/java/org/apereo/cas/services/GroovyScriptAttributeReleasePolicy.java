package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This is {@link GroovyScriptAttributeReleasePolicy} that attempts to release attributes
 * based on the execution result of an external groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    @Serial
    private static final long serialVersionUID = 1703080077563402223L;

    @ExpressionLanguageCapable
    private String groovyScript;

    private Map<String, List<Object>> fetchAttributeValueFromExternalGroovyScript(final String file,
                                                                                  final RegisteredServiceAttributeReleasePolicyContext context,
                                                                                  final Map<String, List<Object>> attributes) throws Throwable {
        val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow();
        val script = cacheMgr.resolveScriptableResource(file, file);
        return script != null
            ? fetchAttributeValueFromScript(script, context, attributes)
            : new HashMap<>();
    }

    protected Map<String, List<Object>> fetchAttributeValueFromScript(
        final ExecutableCompiledScript script,
        final RegisteredServiceAttributeReleasePolicyContext context,
        final Map<String, List<Object>> attributes) throws Throwable {
        val args = new Object[]{attributes, LOGGER, context.getPrincipal(), context.getRegisteredService()};
        val result = (Map<String, List<Object>>) script.execute(args, Map.class, false);
        if (result != null) {
            LOGGER.debug("Attribute release policy returned attributes [{}] from script [{}]", result, groovyScript);
        } else {
            LOGGER.warn("Attribute release policy script [{}] returned null", groovyScript);
        }
        return result;
    }

    @Override
    public Map<String, List<Object>> getAttributesInternal(final RegisteredServiceAttributeReleasePolicyContext context,
                                                           final Map<String, List<Object>> attributes) throws Throwable {
        try {
            LOGGER.debug("Invoking Groovy script with attributes=[{}], principal=[{}], service=[{}] and default logger",
                attributes, context.getPrincipal(), context.getRegisteredService());
            return fetchAttributeValueFromExternalGroovyScript(groovyScript, context, attributes);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.warn("Groovy script [{}] does not exist or cannot be loaded", groovyScript);
        return new HashMap<>();
    }
}
