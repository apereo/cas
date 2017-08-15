package org.apereo.cas.services;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;

/**
 * Resolves the username for the service to be the default principal id.
 *
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class GroovyRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {

    private static final long serialVersionUID = 5823989148794052951L;

    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyRegisteredServiceUsernameProvider.class);

    private String groovyScript;

    public GroovyRegisteredServiceUsernameProvider() {
    }

    public GroovyRegisteredServiceUsernameProvider(final String groovyScript) {
        this.groovyScript = groovyScript;
    }

    @Override
    public String resolveUsernameInternal(final Principal principal, final Service service, final RegisteredService registeredService) {
        final Matcher matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(this.groovyScript);
        final Matcher matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(this.groovyScript);

        if (matcherInline.find()) {
            return resolveUsernameFromInlineGroovyScript(principal, service, matcherInline.group(1));
        }

        if (matcherFile.find()) {
            return resolveUsernameFromExternalGroovyScript(principal, service, matcherFile.group(1));
        }

        LOGGER.warn("Groovy script [{}] is not valid. CAS will switch to use the default principal identifier [{}]",
                this.groovyScript, principal.getId());
        return principal.getId();
    }

    private String resolveUsernameFromExternalGroovyScript(final Principal principal, final Service service,
                                                           final String scriptFile) {
        try {
            LOGGER.debug("Found groovy script to execute");
            final AbstractResource resourceFrom = ResourceUtils.getResourceFrom(scriptFile);
            final String script = IOUtils.toString(resourceFrom.getInputStream(), StandardCharsets.UTF_8);

            final Object result = getGroovyAttributeValue(principal, script);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, scriptFile);
                return result.toString();
            }
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.warn("Groovy script [{}] returned no value for username attribute. Fallback to default [{}]",
                this.groovyScript, principal.getId());
        return principal.getId();
    }

    private String resolveUsernameFromInlineGroovyScript(final Principal principal, final Service service, final String script) {
        try {
            LOGGER.debug("Found groovy script to execute [{}]", this.groovyScript);
            final Object result = getGroovyAttributeValue(principal, script);
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, this.groovyScript);
                return result.toString();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        LOGGER.warn("Groovy script [{}] returned no value for username attribute. Fallback to default [{}]",
                this.groovyScript, principal.getId());
        return principal.getId();
    }

    private static Object getGroovyAttributeValue(final Principal principal, final String script) {
        return ScriptingUtils.executeGroovyShellScript(script,
                CollectionUtils.wrap("attributes", principal.getAttributes(),
                        "id", principal.getId(),
                        "logger", LOGGER));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final GroovyRegisteredServiceUsernameProvider rhs = (GroovyRegisteredServiceUsernameProvider) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.groovyScript, rhs.groovyScript)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(groovyScript)
                .toHashCode();
    }

    public String getGroovyScript() {
        return groovyScript;
    }

    public void setGroovyScript(final String groovyScript) {
        this.groovyScript = groovyScript;
    }
}
