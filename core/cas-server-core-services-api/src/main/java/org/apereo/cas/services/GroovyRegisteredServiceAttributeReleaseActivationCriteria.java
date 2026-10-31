package org.apereo.cas.services;

import module java.base;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.CachedScriptResources;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

/**
 * This is {@link GroovyRegisteredServiceAttributeReleaseActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Slf4j
@Accessors(chain = true)
public class GroovyRegisteredServiceAttributeReleaseActivationCriteria implements RegisteredServiceAttributeReleaseActivationCriteria {
    @Serial
    private static final long serialVersionUID = 1942510462696845607L;

    private int order;

    @ExpressionLanguageCapable
    private String groovyScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledScript executableScript;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient String resolvedGroovyScript;

    @Override
    public boolean shouldActivate(final RegisteredServiceAttributeReleasePolicyContext context) {
        resolveScriptIfNeeded();
        return FunctionUtils.doIfNotNull(executableScript, () -> getGroovyScriptResult(context), () -> false).get();
    }

    protected Boolean getGroovyScriptResult(final RegisteredServiceAttributeReleasePolicyContext context) {
        return FunctionUtils.doAndHandle(() -> {
            val args = CollectionUtils.wrap("context", context, "logger", LOGGER);
            executableScript.setBinding(args);
            return executableScript.execute(args.values().toArray(), Boolean.class);
        }, e -> Boolean.FALSE).get();
    }

    /**
     * Resolve the script when it has not been resolved yet, when the cached script was closed because it
     * expired from the script cache, or when the script setting changed since it was resolved. Otherwise
     * the script already held is used, rather than resolving expressions and resources on every evaluation.
     */
    private void resolveScriptIfNeeded() {
        if (executableScript == null || executableScript.isClosed() || !Objects.equals(groovyScript, resolvedGroovyScript)) {
            initializeWatchableScriptIfNeeded();
        }
    }

    @PostLoad
    private void initializeWatchableScriptIfNeeded() {
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        if (scriptFactoryInstance.isExternalScript(groovyScript)) {
            val script = SpringExpressionLanguageValueResolver.getInstance().resolve(
                scriptFactoryInstance.getExternalScript(groovyScript).orElseThrow());
            val resource = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(script));
            this.executableScript = CachedScriptResources.fromResource(resource);
        }
        if (scriptFactoryInstance.isInlineScript(groovyScript) && CasRuntimeHintsRegistrar.notInNativeImage()) {
            val script = scriptFactoryInstance.getInlineScript(groovyScript).orElseThrow();
            this.executableScript = CachedScriptResources.fromInlineScript(script);
        }
        this.resolvedGroovyScript = groovyScript;
    }

}
