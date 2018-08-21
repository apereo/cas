package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.ResourceLoader;

import java.security.GeneralSecurityException;
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
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(script);
        val ex = getScriptExecutionResult(auth, matcherInline);

        if (ex != null && ex.isPresent()) {
            throw new GeneralSecurityException(ex.get());
        }
        return true;
    }

    private Optional<Exception> getScriptExecutionResult(final Authentication auth, final Matcher matcherInline) {
        if (matcherInline.find()) {
            val args = CollectionUtils.wrap("principal", auth.getPrincipal(), "logger", LOGGER);
            val inlineScript = matcherInline.group(1);
            return ScriptingUtils.executeGroovyShellScript(inlineScript, args, Optional.class);
        }
        val res = this.resourceLoader.getResource(script);
        final Object[] args = {auth.getPrincipal(), LOGGER};
        return ScriptingUtils.executeGroovyScript(res, args, Optional.class, true);
    }
}
