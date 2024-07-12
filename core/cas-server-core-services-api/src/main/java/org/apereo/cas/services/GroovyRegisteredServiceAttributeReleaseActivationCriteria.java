package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import java.io.Serial;

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

    @Override
    public boolean shouldActivate(final RegisteredServiceAttributeReleasePolicyContext context) {
        initializeWatchableScriptIfNeeded();
        return FunctionUtils.doIfNotNull(executableScript, () -> getGroovyScriptResult(context), () -> false).get();
    }

    protected Boolean getGroovyScriptResult(final RegisteredServiceAttributeReleasePolicyContext context) {
        return FunctionUtils.doAndHandle(() -> {
            val args = CollectionUtils.wrap("context", context, "logger", LOGGER);
            executableScript.setBinding(args);
            return executableScript.execute(args.values().toArray(), Boolean.class);
        }, e -> Boolean.FALSE).get();
    }

    @PostLoad
    private void initializeWatchableScriptIfNeeded() {
        val scriptFactoryInstance = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        if (scriptFactoryInstance.isExternalScript(groovyScript)) {
            val script = SpringExpressionLanguageValueResolver.getInstance().resolve(
                scriptFactoryInstance.getExternalScript(groovyScript).orElseThrow());
            val resource = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(script));
            this.executableScript = scriptFactoryInstance.fromResource(resource);
        }
        if (scriptFactoryInstance.isInlineScript(groovyScript) && CasRuntimeHintsRegistrar.notInNativeImage()) {
            val script = scriptFactoryInstance.getInlineScript(groovyScript).orElseThrow();
            this.executableScript = scriptFactoryInstance.fromScript(script);
        }
    }

}
