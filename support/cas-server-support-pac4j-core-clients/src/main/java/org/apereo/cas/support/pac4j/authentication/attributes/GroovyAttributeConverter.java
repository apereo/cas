package org.apereo.cas.support.pac4j.authentication.attributes;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledGroovyScript;
import org.apereo.cas.util.scripting.ScriptingUtils;
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
    private ExecutableCompiledGroovyScript script;

    public GroovyAttributeConverter() {
        super(Serializable.class);
    }

    @Override
    public synchronized Object convert(final Object attribute) {
        if (script != null) {
            val args = CollectionUtils.wrap("attribute", attribute, "logger", LOGGER);
            script.setBinding(args);
            return script.execute(args.values().toArray(), Object.class, false);
        }
        return attribute;
    }

    @Override
    public synchronized Boolean accept(final String typeName) {
        val matcherInline = ScriptingUtils.getMatcherForInlineGroovyScript(typeName);
        if (matcherInline.find()) {
            val inlineGroovy = matcherInline.group(1);
            val cacheMgr = ApplicationContextProvider.getScriptResourceCacheManager().orElseThrow(() ->
                new RuntimeException("No Groovy script cache manager is available"));
            this.script = cacheMgr.resolveScriptableResource(inlineGroovy, inlineGroovy);
            return true;
        }
        return false;
    }
}
