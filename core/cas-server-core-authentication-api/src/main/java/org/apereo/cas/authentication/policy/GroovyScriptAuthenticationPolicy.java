package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import javax.persistence.Transient;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GroovyScriptAuthenticationPolicy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@NoArgsConstructor(force = true)
@EqualsAndHashCode(callSuper = true)
@Setter
@Getter
@AllArgsConstructor
public class GroovyScriptAuthenticationPolicy extends BaseAuthenticationPolicy {

    private static final long serialVersionUID = 6948477763790549040L;

    private String script;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient WatchableGroovyScriptResource executableScript;

    public GroovyScriptAuthenticationPolicy(final String script) {
        this.script = script;
    }

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(final Authentication auth,
                                                             final Set<AuthenticationHandler> authenticationHandlers,
                                                             final ConfigurableApplicationContext applicationContext,
                                                             final Optional<Serializable> assertion) throws Exception {
        initializeWatchableScriptIfNeeded();
        val ex = getScriptExecutionResult(auth);
        if (ex != null && ex.isPresent()) {
            throw new GeneralSecurityException(ex.get());
        }
        return AuthenticationPolicyExecutionResult.success();
    }

    @Override
    public boolean shouldResumeOnFailure(final Throwable failure) {
        initializeWatchableScriptIfNeeded();
        val args = CollectionUtils.wrap("failure", failure, "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute("shouldResumeOnFailure", Boolean.class, args.values().toArray());
    }

    @SneakyThrows
    private void initializeWatchableScriptIfNeeded() {
        if (this.executableScript == null) {
            val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(script);
            if (!matcherFile.find()) {
                throw new IllegalArgumentException("Unable to locate groovy script file at " + script);
            }
            val resource = ResourceUtils.getRawResourceFrom(matcherFile.group(2));
            this.executableScript = new WatchableGroovyScriptResource(resource);
        }
    }

    private Optional<Exception> getScriptExecutionResult(final Authentication auth) {
        val args = CollectionUtils.wrap("principal", auth.getPrincipal(), "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Optional.class);
    }
}
