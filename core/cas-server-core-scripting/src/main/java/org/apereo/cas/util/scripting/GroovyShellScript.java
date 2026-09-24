package org.apereo.cas.util.scripting;

import module java.base;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

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
@Accessors(chain = true)
public class GroovyShellScript implements ExecutableCompiledScript {
    private static final ThreadLocal<@Nullable ScriptBinding> BINDING_THREAD_LOCAL = new ThreadLocal<>();

    private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();

    private final long instanceId = INSTANCE_COUNTER.incrementAndGet();

    private final CasReentrantLock lock = new CasReentrantLock();
    private final String script;

    @Nullable
    private Script compiledScript;

    /**
     * Unlike {@link WatchableGroovyScriptResource}, this defaults to {@code false} because that is the
     * behavior inline scripts have always had: a broken inline script yields no value and the caller
     * carries on. Several features rely on that, such as a mapped attribute release policy that releases
     * the attributes whose scripts did work. Callers that need a failure reported pass {@code true} to
     * {@link #execute(Object[], Class, boolean)} or call {@link #setFailOnError(boolean)}.
     */
    @Setter
    private boolean failOnError;

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz) throws Throwable {
        return execute(args, clazz, failOnError);
    }

    @Override
    public void execute(final Object[] args) throws Throwable {
        execute(args, Void.class, failOnError);
    }

    @Override
    public <T> @Nullable T execute(final Object[] args, final Class<T> clazz, final boolean failOnError) {
        val binding = consumeBinding();
        return lock.execute(() -> {
            try {
                LOGGER.trace("Beginning to execute script [{}]", this);
                if (compiledScript == null) {
                    compiledScript = ScriptingUtils.parseGroovyShellScript(binding, script);
                }
                val currentScript = Objects.requireNonNull(compiledScript);
                if (binding != null && !binding.isEmpty()) {
                    LOGGER.trace("Setting binding [{}]", binding);
                    currentScript.setBinding(new Binding(binding));
                }
                LOGGER.trace("Current binding [{}]", currentScript.getBinding());
                val result = ScriptingUtils.executeGroovyShellScript(currentScript, clazz, failOnError);
                LOGGER.debug("Groovy script [{}] returns result [{}]", this, result);
                return result;
            } catch (final GroovyRuntimeException e) {
                if (failOnError) {
                    throw e;
                }
                LoggingUtils.error(LOGGER, e);
                return null;
            } finally {
                if (compiledScript != null) {
                    compiledScript.setBinding(new Binding());
                }
                LOGGER.trace("Completed script execution [{}]", this);
            }
        });
    }

    @Override
    public <T> @Nullable T execute(final String methodName, final Class<T> clazz, final Object... args) throws Throwable {
        return execute(args, clazz);
    }

    /**
     * {@inheritDoc}
     * The binding is held for the calling thread only, is tied to this script, and is
     * consumed by the next {@link #execute(Object[], Class, boolean)} on that same thread,
     * which always removes it. Callers must invoke {@code execute} on the thread that
     * assigned the binding, so that request state cannot be observed by a later execution
     * or by an unrelated script.
     */
    @Override
    public void setBinding(final Map<String, Object> args) {
        BINDING_THREAD_LOCAL.set(new ScriptBinding(instanceId, new HashMap<>(args)));
    }

    /**
     * Removes the binding assigned to the current thread and returns it when it was
     * assigned to this script. A binding left behind by another script, or by an attempt
     * that never executed, is discarded rather than handed to this execution.
     *
     * @return the variables assigned to this script on the current thread, if any
     */
    private @Nullable Map<String, Object> consumeBinding() {
        val assigned = BINDING_THREAD_LOCAL.get();
        BINDING_THREAD_LOCAL.remove();
        return assigned != null && assigned.owner() == instanceId ? assigned.variables() : null;
    }

    private record ScriptBinding(long owner, Map<String, Object> variables) {
    }

    @Override
    public Resource getResource() {
        return new ByteArrayResource(
            script.getBytes(StandardCharsets.UTF_8),
            DigestUtils.abbreviate(script));
    }
}
