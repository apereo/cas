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
 * This is {@link GroovyRegisteredServiceAccessStrategyActivationCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Slf4j
@EqualsAndHashCode(of = {"order", "groovyScript"})
public class GroovyRegisteredServiceAccessStrategyActivationCriteria implements RegisteredServiceAccessStrategyActivationCriteria {
    @Serial
    private static final long serialVersionUID = 5228603912161923218L;

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
    public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        resolveScriptIfNeeded();
        val result = getGroovyAttributeValue(request);
        if (result == null) {
            throw new IllegalStateException(
                "Groovy script [%s] produced no result to decide whether the access strategy should activate"
                    .formatted(groovyScript));
        }
        return result;
    }

    protected Boolean getGroovyAttributeValue(final RegisteredServiceAccessStrategyRequest request) throws Throwable {
        val args = CollectionUtils.wrap("accessRequest", request, "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Boolean.class);
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
