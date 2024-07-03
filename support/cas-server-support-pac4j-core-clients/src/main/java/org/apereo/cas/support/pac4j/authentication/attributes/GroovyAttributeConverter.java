package org.apereo.cas.support.pac4j.authentication.attributes;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.profile.converter.AbstractAttributeConverter;
import java.io.Serializable;

/**
 * This is {@link GroovyAttributeConverter}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
public class GroovyAttributeConverter extends AbstractAttributeConverter {
    private ExecutableCompiledScript script;

    public GroovyAttributeConverter() {
        super(Serializable.class);
    }

    public GroovyAttributeConverter(final ExecutableCompiledScript script) {
        this();
        this.script = script;
    }

    @Override
    public Object convert(final Object attribute) {
        if (script != null) {
            return FunctionUtils.doUnchecked(() -> {
                val args = CollectionUtils.wrap("attribute", attribute, "logger", LOGGER);
                script.setBinding(args);
                return script.execute(args.values().toArray(), Object.class, false);
            });
        }
        return attribute;
    }

    @Override
    public Boolean accept(final String typeName) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        if (scriptFactory.isInlineScript(typeName)) {
            val inlineGroovy = scriptFactory.getInlineScript(typeName).orElseThrow();
            val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow(() ->
                new RuntimeException("No Groovy script cache manager is available"));
            this.script = cacheMgr.resolveScriptableResource(inlineGroovy, inlineGroovy);
            return true;
        }
        return false;
    }
}
