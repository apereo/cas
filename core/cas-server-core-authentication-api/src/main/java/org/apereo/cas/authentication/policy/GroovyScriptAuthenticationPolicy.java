package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.persistence.Transient;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * This is {@link GroovyScriptAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyScriptAuthenticationPolicy implements AuthenticationPolicy {

    private final String script;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledGroovyScript executableScript;

    public GroovyScriptAuthenticationPolicy(final String script) {
        this.script = script;
        initializeWatchableScriptIfNeeded();
    }

    @Override
    public boolean isSatisfiedBy(final Authentication auth, final Set<AuthenticationHandler> authenticationHandlers) throws Exception {
        initializeWatchableScriptIfNeeded();

        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(script);
        val ex = getScriptExecutionResult(auth, matcherInline);

        if (ex != null && ex.isPresent()) {
            throw new GeneralSecurityException(ex.get());
        }
        return true;
    }

    @SneakyThrows
    private void initializeWatchableScriptIfNeeded() {
        if (this.executableScript == null) {
            val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(script);
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(script);

            if (matcherFile.find()) {
                val resource = ResourceUtils.getRawResourceFrom(matcherFile.group(2));
                this.executableScript = new WatchableGroovyScriptResource(resource);
            } else if (matcherInline.find()) {
                this.executableScript = new GroovyShellScript(matcherInline.group(1));
            }
        }
    }

    private Optional<Exception> getScriptExecutionResult(final Authentication auth, final Matcher matcherInline) {
        val args = CollectionUtils.wrap("principal", auth.getPrincipal(), "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Optional.class);
    }
}
