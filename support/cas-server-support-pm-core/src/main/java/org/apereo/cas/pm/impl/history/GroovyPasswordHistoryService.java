package org.apereo.cas.pm.impl.history;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyPasswordHistoryService extends BasePasswordHistoryService {
    private final ExecutableCompiledScript watchableScript;

    public GroovyPasswordHistoryService(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) throws Throwable {
        return Boolean.TRUE.equals(watchableScript.execute("exists", Boolean.class, changeRequest, LOGGER));
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) throws Throwable {
        return Boolean.TRUE.equals(watchableScript.execute("store", Boolean.class, changeRequest, LOGGER));
    }

    @Override
    public @Nullable Collection<? extends PasswordHistoryEntity> fetchAll() throws Throwable {
        return watchableScript.execute("fetchAll", Collection.class, LOGGER);
    }

    @Override
    public @Nullable Collection<? extends PasswordHistoryEntity> fetch(final String username) throws Throwable {
        return watchableScript.execute("fetch", Collection.class, username, LOGGER);
    }

    @Override
    public void remove(final String username) throws Throwable {
        watchableScript.execute("remove", Void.class, username, LOGGER);
    }

    @Override
    public void removeAll() throws Throwable {
        watchableScript.execute("removeAll", Void.class, LOGGER);
    }
}
