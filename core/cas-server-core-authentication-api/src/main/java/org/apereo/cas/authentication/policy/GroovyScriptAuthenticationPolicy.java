package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.security.GeneralSecurityException;

/**
 * This is {@link GroovyScriptAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyScriptAuthenticationPolicy implements AuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptAuthenticationPolicy.class);

    private final ResourceLoader resourceLoader;
    private final String script;

    public GroovyScriptAuthenticationPolicy(final ResourceLoader resourceLoader, final String script) {
        this.resourceLoader = resourceLoader;
        this.script = script;
    }

    @Override
    public boolean isSatisfiedBy(final Authentication auth) throws Exception {
        final Exception ex;
        if (ScriptingUtils.isInlineGroovyScript(script)) {
            ex = ScriptingUtils.executeGroovyShellScript(script,
                    CollectionUtils.wrap("principal", auth.getPrincipal(),
                            "logger", LOGGER));
        } else {
            final Resource res = this.resourceLoader.getResource(script);
            ex = ScriptingUtils.executeGroovyScript(res,
                    new Object[]{auth.getPrincipal(), LOGGER}, Exception.class);
        }

        if (ex != null) {
            throw new GeneralSecurityException(ex);
        }
        return true;
    }
}
