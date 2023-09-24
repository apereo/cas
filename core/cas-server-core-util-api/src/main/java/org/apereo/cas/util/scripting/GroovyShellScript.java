package org.apereo.cas.util.scripting;

import org.apereo.cas.util.concurrent.CasReentrantLock;
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
    private static final ThreadLocal<Map<String, Object>> BINDING_THREAD_LOCAL = new ThreadLocal<>();

    private final CasReentrantLock lock = new CasReentrantLock();
    private final Script groovyScript;
    private final String script;

    public GroovyShellScript(final String script) {
        this.script = script;
        this.groovyScript = ScriptingUtils.parseGroovyShellScript(script);
    }

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz) throws Throwable {
        return execute(args, clazz, true);
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        execute(args, Void.class, true);
    }

    @Override
    public <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) throws Throwable {
        if (lock.tryLock()) {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                val binding = BINDING_THREAD_LOCAL.get();
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
            } finally {
                BINDING_THREAD_LOCAL.remove();
                if (groovyScript != null) {
                    groovyScript.setBinding(new Binding(Map.of()));
                }
                LOGGER.trace("Completed script execution [{}]", this);
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public <T> T execute(final String methodName, final Class<T> clazz, final Object... args) throws Throwable {
        return execute(args, clazz);
    }

    @Override
    public void setBinding(final Map<String, Object> args) {
        BINDING_THREAD_LOCAL.set(new HashMap<>(args));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("script", script)
            .toString();
    }
}
