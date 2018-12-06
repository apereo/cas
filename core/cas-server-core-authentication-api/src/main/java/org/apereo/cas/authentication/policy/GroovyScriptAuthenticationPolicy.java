package org.apereo.cas.authentication.policy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

/**
 * This is {@link GroovyScriptAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyScriptAuthenticationPolicy implements AuthenticationPolicy {
    private final ResourceLoader resourceLoader;
    private final String script;

    @Override
    public boolean isSatisfiedBy(final Authentication auth) throws Exception {
        final Optional<Exception> ex;
        final Matcher matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(script);
        if (matcherInline.find()) {
            final Map<String, Object> args = CollectionUtils.wrap("principal", auth.getPrincipal(), "logger", LOGGER);
            final String inlineScript = matcherInline.group(1);
            ex = ScriptingUtils.executeGroovyShellScript(inlineScript, args, Optional.class);
        } else {
            final Resource res = this.resourceLoader.getResource(script);
            final Object[] args = {auth.getPrincipal(), LOGGER};
            ex = ScriptingUtils.executeGroovyScript(res, args, Optional.class);
        }

        if (ex != null && ex.isPresent()) {
            throw new GeneralSecurityException(ex.get());
        }
        return true;
    }
}
