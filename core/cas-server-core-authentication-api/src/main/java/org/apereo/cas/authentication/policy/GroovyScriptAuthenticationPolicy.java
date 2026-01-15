package org.apereo.cas.authentication.policy;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationPolicyExecutionResult;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.Assert;
import jakarta.persistence.Transient;

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
@RequiredArgsConstructor
@Accessors(chain = true)
public class GroovyScriptAuthenticationPolicy extends BaseAuthenticationPolicy {

    @Serial
    private static final long serialVersionUID = 6948477763790549040L;

    private final String script;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    @Nullable
    private transient ExecutableCompiledScript executableScript;

    @Override
    public AuthenticationPolicyExecutionResult isSatisfiedBy(
        @Nullable final Authentication authentication,
        final Set<AuthenticationHandler> authenticationHandlers,
        final ConfigurableApplicationContext applicationContext,
        final Map<String, ? extends Serializable> context) throws Throwable {

        if (authentication == null) {
            LOGGER.warn("Authentication attempt is null and cannot satisfy policy");
            return AuthenticationPolicyExecutionResult.failure();
        }

        
        initializeWatchableScriptIfNeeded();

        val args = CollectionUtils.<String, Object>wrap(
            "authentication", authentication,
            "context", context,
            "applicationContext", applicationContext,
            "logger", LOGGER);
        Objects.requireNonNull(executableScript).setBinding(args);
        val ex = executableScript.execute(args.values().toArray(), Optional.class);
        if (ex != null && ex.isPresent()) {
            val exception = (Exception) ex.get();
            throw new GeneralSecurityException(exception);
        }
        return AuthenticationPolicyExecutionResult.success();
    }

    @Override
    public boolean shouldResumeOnFailure(final Throwable failure) {
        val supplier = Unchecked.supplier(() -> {
            initializeWatchableScriptIfNeeded();
            val args = CollectionUtils.wrap("failure", failure, "logger", LOGGER);
            Objects.requireNonNull(executableScript).setBinding(args);
            return Boolean.TRUE.equals(executableScript.execute("shouldResumeOnFailure",
                Boolean.class, args.values().toArray()));
        });
        val result = supplier.get();
        Assert.notNull(result, "Authentication policy result cannot be null");
        return result;
    }

    private void initializeWatchableScriptIfNeeded() throws Exception {
        if (this.executableScript == null) {
            val resource = ResourceUtils.getRawResourceFrom(script);
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            this.executableScript = scriptFactory.fromResource(resource);
        }
    }

}
