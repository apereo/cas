package org.apereo.cas.services;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
@AllArgsConstructor
public class GroovyRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 5823989148794052951L;

    private String groovyScript;

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        final var matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(this.groovyScript);
        final var matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(this.groovyScript);
        if (matcherInline.find()) {
            return resolveUsernameFromInlineGroovyScript(principal, service, matcherInline.group(1));
        }
        if (matcherFile.find()) {
            return resolveUsernameFromExternalGroovyScript(principal, service, matcherFile.group(1));
        }
        LOGGER.warn("Groovy script [{}] is not valid. CAS will switch to use the default principal identifier [{}]", this.groovyScript, principal.getId());
        return principal.getId();
    }

    private String resolveUsernameFromExternalGroovyScript(final Principal principal, final Service service, final String scriptFile) {
        try {
            LOGGER.debug("Found groovy script to execute");
            final var resourceFrom = ResourceUtils.getResourceFrom(scriptFile);
            final var script = IOUtils.toString(resourceFrom.getInputStream(), StandardCharsets.UTF_8);
            final var result = getGroovyAttributeValue(principal, script);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, scriptFile);
                return result.toString();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] returned no value for username attribute. Fallback to default [{}]", this.groovyScript, principal.getId());
        return principal.getId();
    }

    private String resolveUsernameFromInlineGroovyScript(final Principal principal, final Service service, final String script) {
        try {
            LOGGER.debug("Found groovy script to execute [{}]", this.groovyScript);
            final var result = getGroovyAttributeValue(principal, script);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, this.groovyScript);
                return result.toString();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Groovy script [{}] returned no value for username attribute. Fallback to default [{}]", this.groovyScript, principal.getId());
        return principal.getId();
    }

    private static Object getGroovyAttributeValue(final Principal principal, final String script) {
        final Map<String, Object> args = CollectionUtils.wrap("attributes", principal.getAttributes(), "id", principal.getId(), "logger", LOGGER);
        return ScriptingUtils.executeGroovyShellScript(script, args, Object.class);
    }

}
