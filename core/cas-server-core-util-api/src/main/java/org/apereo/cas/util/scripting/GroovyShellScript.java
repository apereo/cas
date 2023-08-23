package org.apereo.cas.util.scripting;

import groovy.lang.Binding;
import groovy.lang.Script;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is {@link GroovyShellScript}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Slf4j
public class GroovyShellScript implements ExecutableCompiledGroovyScript {
    private final ReentrantLock executionLock = new ReentrantLock();

    private final Script groovyScript;

    private final String script;

    @Setter
    private Map<String, Object> binding = new HashMap<>();

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
    public <T> T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        executionLock.lock();
        try {
            LOGGER.trace("Beginning to execute script [{}]", this);
            if (groovyScript != null) {
                if (binding != null && !binding.isEmpty()) {
                    groovyScript.setBinding(new Binding(binding));
                }
                return ScriptingUtils.executeGroovyShellScript(groovyScript, clazz);
            }
            return null;
        } finally {
            executionLock.unlock();
            LOGGER.trace("Completed script execution [{}]", this);
        }
    }

    @Override
    public <T> T execute(final String methodName, final Class<T> clazz, final Object... args) {
        return execute(args, clazz);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("script", script)
            .append("executionLock", executionLock)
            .toString();
    }
}
