package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link ScriptedRegisteredServiceUsernameProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ScriptedRegisteredServiceUsernameProvider extends BaseRegisteredServiceUsernameAttributeProvider {
    private static final long serialVersionUID = -678554831202936052L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptedRegisteredServiceUsernameProvider.class);

    private String script;

    public ScriptedRegisteredServiceUsernameProvider() {
    }

    public ScriptedRegisteredServiceUsernameProvider(final String script) {
        this.script = script;
    }

    @Override
    protected String resolveUsernameInternal(final Principal principal, final Service service) {
        try {
            LOGGER.debug("Found groovy script to execute");
            final Object result = ScriptingUtils.executeGroovyScriptEngine(this.script,
                    new Object[] {principal.getAttributes(), principal.getId(), LOGGER});
            if (result != null) {
                LOGGER.debug("Found username [{}] from script [{}]", result, this.script);
                return result.toString();
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Script [{}] returned no value for username attribute. Fallback to default [{}]", this.script, principal.getId());
        return principal.getId();
    }

    public String getScript() {
        return script;
    }

    public void setScript(final String script) {
        this.script = script;
    }
}
