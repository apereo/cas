package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.GroovyShellScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
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
    private transient ExecutableCompiledGroovyScript executableScript;

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
        val matcherFile = ScriptingUtils.getMatcherForExternalGroovyScript(groovyScript);
        if (matcherFile.find()) {
            val script = SpringExpressionLanguageValueResolver.getInstance().resolve(matcherFile.group());
            val resource = FunctionUtils.doUnchecked(() -> ResourceUtils.getRawResourceFrom(script));
            this.executableScript = new WatchableGroovyScriptResource(resource);
        }
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(groovyScript);
        if (matcherInline.find() && CasRuntimeHintsRegistrar.notInNativeImage()) {
            this.executableScript = new GroovyShellScript(matcherInline.group(1));
        }
    }

}
