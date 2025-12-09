package org.apereo.cas.util.scripting;

import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.nio.charset.StandardCharsets;
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
@RequiredArgsConstructor
@ToString(of = "script")
public class GroovyShellScript implements ExecutableCompiledScript {
    private static final ThreadLocal<Map<String, Object>> BINDING_THREAD_LOCAL = new ThreadLocal<>();

    private final CasReentrantLock lock = new CasReentrantLock();
    private final String script;

    @Nullable
    private Script compiledScript;

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz) throws Throwable {
        return execute(args, clazz, true);
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        execute(args, Void.class, true);
    }

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        if (lock.tryLock()) {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                val binding = BINDING_THREAD_LOCAL.get();
                if (compiledScript == null) {
                    compiledScript = ScriptingUtils.parseGroovyShellScript(binding, script);
                }
                if (binding != null && !binding.isEmpty()) {
                    LOGGER.trace("Setting binding [{}]", binding);
                    compiledScript.setBinding(new Binding(binding));
                }
                LOGGER.trace("Current binding [{}]", compiledScript.getBinding());
                val result = ScriptingUtils.executeGroovyShellScript(compiledScript, clazz);
                LOGGER.debug("Groovy script [{}] returns result [{}]", this, result);
                return result;
            } catch (final GroovyRuntimeException e) {
                LoggingUtils.error(LOGGER, e);
            } finally {
                BINDING_THREAD_LOCAL.remove();
                if (compiledScript != null) {
                    compiledScript.setBinding(new Binding(Map.of()));
                }
                LOGGER.trace("Completed script execution [{}]", this);
                lock.unlock();
            }
        }
        return null;
    }

    @Override
    public <T> @Nullable T execute(final String methodName, final Class<T> clazz, final Object... args) throws Throwable {
        return execute(args, clazz);
    }

    @Override
    public void setBinding(final Map<String, Object> args) {
        BINDING_THREAD_LOCAL.set(new HashMap<>(args));
    }

    @Override
    public Resource getResource() {
        return new ByteArrayResource(script.getBytes(StandardCharsets.UTF_8));
    }
}
