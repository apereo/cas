package org.apereo.cas.authentication.policy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * This is {@link GroovyScriptAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@AllArgsConstructor
public class GroovyScriptAuthenticationPolicy implements AuthenticationPolicy {
    private final ResourceLoader resourceLoader;
    private final String script;

    @Override
    public boolean isSatisfiedBy(final Authentication auth) throws Exception {
        final Exception ex;
        if (ScriptingUtils.isInlineGroovyScript(script)) {
            final Map<String, Object> args = CollectionUtils.wrap("principal", auth.getPrincipal(), "logger", LOGGER);
            ex = ScriptingUtils.executeGroovyShellScript(script, args, Exception.class);
        } else {
            final Resource res = this.resourceLoader.getResource(script);
            final Object[] args = {auth.getPrincipal(), LOGGER};
            ex = ScriptingUtils.executeGroovyScript(res, args, Exception.class);
        }

        if (ex != null) {
            throw new GeneralSecurityException(ex);
        }
        return true;
    }
}
