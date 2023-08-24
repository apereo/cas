package org.apereo.cas.util.scripting;

import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.builder.ToStringBuilder;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link GroovyShellScript}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Slf4j
public class GroovyShellScript implements ExecutableCompiledGroovyScript {
    private final Script groovyScript;
    private final String script;

    private final ThreadLocal<Map<String, Object>> bindingThreadLocal = new ThreadLocal<>();

    public GroovyShellScript(final String script) {
        this.script = script;
        this.groovyScript = ScriptingUtils.parseGroovyShellScript(script);
    }

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz) {
        return execute(args, clazz, true);
    }

    @Override
    public void execute(final Object[] args) {
        execute(args, Void.class, true);
    }

    @Override
    public synchronized <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        try {
            LOGGER.trace("Beginning to execute script [{}]", this);
            val binding = bindingThreadLocal.get();
            if (groovyScript != null) {
                if (binding != null && !binding.isEmpty()) {
                    LOGGER.trace("Setting binding [{}]", binding);
                    groovyScript.setBinding(new Binding(binding));
                }
                LOGGER.trace("Current binding [{}]", groovyScript.getBinding());
                val result = ScriptingUtils.executeGroovyShellScript(groovyScript, clazz);
                LOGGER.debug("Groovy script [{}] returns result [{}]", this, result);
                return result;
            }
            return null;
        } finally {
            bindingThreadLocal.remove();
            if (groovyScript != null) {
                groovyScript.setBinding(new Binding(Map.of()));
            }
            LOGGER.trace("Completed script execution [{}]", this);
        }
    }

    @Override
    public <T> T execute(final String methodName, final Class<T> clazz, final Object... args) {
        return execute(args, clazz);
    }

    @Override
    public void setBinding(final Map<String, Object> args) {
        bindingThreadLocal.set(new HashMap<>(args));
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("script", script)
            .toString();
    }
}
