package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;
import lombok.NoArgsConstructor;

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
public class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

    private static final long serialVersionUID = 1703080077563402223L;

    private String groovyScript;

    @Override
    public Map<String, Object> getAttributesInternal(final Principal principal, final Map<String, Object> attributes, final RegisteredService service) {
        try {
            final Object[] args = {attributes, LOGGER, principal, service};
            final Resource resource = ResourceUtils.getResourceFrom(this.groovyScript);
            return ScriptingUtils.executeGroovyScript(resource, args, Map.class);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] does not exist or cannot be loaded", groovyScript);
        return new HashMap<>(0);
    }
}
