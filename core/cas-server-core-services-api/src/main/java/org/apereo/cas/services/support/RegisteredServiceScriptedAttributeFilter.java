package org.apereo.cas.services.support;

import org.apereo.cas.services.RegisteredServiceAttributeFilter;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.nativex.CasRuntimeHintsRegistrar;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;
import java.io.Serial;
import java.util.List;
import java.util.Map;

/**
 * This is {@link RegisteredServiceScriptedAttributeFilter}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class RegisteredServiceScriptedAttributeFilter implements RegisteredServiceAttributeFilter {

    @Serial
    private static final long serialVersionUID = 122972056984610198L;

    private int order;

    private String script;

    @JsonIgnore
    @Transient
    @org.springframework.data.annotation.Transient
    private transient ExecutableCompiledScript executableScript;

    @JsonCreator
    public RegisteredServiceScriptedAttributeFilter(@JsonProperty("order") final int order,
                                                    @JsonProperty("script") final String script) {
        this.order = order;
        this.script = script;
    }

    @Override
    public Map<String, List<Object>> filter(final Map<String, List<Object>> givenAttributes) throws Throwable {
        initializeWatchableScriptIfNeeded();
        return getGroovyAttributeValue(givenAttributes);
    }

    @PostLoad
    private void initializeWatchableScriptIfNeeded() {
        if (this.executableScript == null) {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            if (scriptFactory.isExternalScript(script)) {
                val resource = FunctionUtils.doUnchecked(() -> {
                    val scriptFile = SpringExpressionLanguageValueResolver.getInstance()
                        .resolve(scriptFactory.getExternalScript(script).orElseThrow());
                    LOGGER.debug("Loading attribute filter groovy script from [{}]", scriptFile);
                    return ResourceUtils.getRawResourceFrom(scriptFile);
                });
                this.executableScript = scriptFactory.fromResource(resource);
            } else if (scriptFactory.isInlineScript(script) && CasRuntimeHintsRegistrar.notInNativeImage()) {
                this.executableScript = scriptFactory.fromScript(scriptFactory.getInlineScript(script).orElseThrow());
            }
        }
    }

    private Map<String, List<Object>> getGroovyAttributeValue(final Map<String, List<Object>> resolvedAttributes) throws Throwable {
        val args = CollectionUtils.wrap("attributes", resolvedAttributes, "logger", LOGGER);
        executableScript.setBinding(args);
        return executableScript.execute(args.values().toArray(), Map.class);
    }

}
