package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link GroovyScriptAttributeReleasePolicy} that attempts to release attributes
 * based on the execution result of an external groovy script.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptAttributeReleasePolicy.class);
    private static final long serialVersionUID = 1703080077563402223L;

    private String groovyScript;

    public GroovyScriptAttributeReleasePolicy() {
        
    }

    public GroovyScriptAttributeReleasePolicy(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    protected Map<String, Object> getAttributesInternal(final Principal principal,
                                                        final Map<String, Object> attributes,
                                                        final RegisteredService service) {
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
