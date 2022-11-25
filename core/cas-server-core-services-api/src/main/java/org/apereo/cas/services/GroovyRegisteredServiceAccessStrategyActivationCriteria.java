package org.apereo.cas.services;

import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
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
    private transient ExecutableCompiledGroovyScript executableScript;

    @Override
    public boolean shouldActivate(final RegisteredServiceAccessStrategyRequest request) {
        initializeWatchableScriptIfNeeded();
        return getGroovyAttributeValue(request);
    }

    protected Boolean getGroovyAttributeValue(final RegisteredServiceAccessStrategyRequest request) {
        val args = CollectionUtils.wrap("accessRequest", request, "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Boolean.class);
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
        if (matcherInline.find()) {
            this.executableScript = new GroovyShellScript(matcherInline.group(1));
        }
    }
}
