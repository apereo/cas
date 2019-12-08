package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 1703080077563402223L;

    private String groovyScript;

    @Override
    public Map<String, List<Object>> getAttributesInternal(final Principal principal, final Map<String, List<Object>> attributes,
                                                           final RegisteredService registeredService, final Service selectedService) {
        try {
            val args = new Object[]{attributes, LOGGER, principal, registeredService};
            LOGGER.debug("Invoking Groovy script with attributes=[{}], principal=[{}], service=[{}] and default logger",
                attributes, principal, registeredService);
            val resource = ResourceUtils.getResourceFrom(SpringExpressionLanguageValueResolver.getInstance().resolve(this.groovyScript));
            return ScriptingUtils.executeGroovyScript(resource, args, Map.class, true);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] does not exist or cannot be loaded", groovyScript);
        return new HashMap<>(0);
    }
}
