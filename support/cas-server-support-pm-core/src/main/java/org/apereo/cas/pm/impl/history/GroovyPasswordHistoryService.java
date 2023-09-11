package org.apereo.cas.pm.impl.history;

import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.Collection;

/**
 * This is {@link GroovyPasswordHistoryService}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public class GroovyPasswordHistoryService extends BasePasswordHistoryService {
    private final WatchableGroovyScriptResource watchableScript;

    public GroovyPasswordHistoryService(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) throws Throwable {
        return this.watchableScript.execute("exists", Boolean.class, changeRequest, LOGGER);
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) throws Throwable {
        return this.watchableScript.execute("store", Boolean.class, changeRequest, LOGGER);
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetchAll() throws Throwable {
        return this.watchableScript.execute("fetchAll", Collection.class, LOGGER);
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetch(final String username) throws Throwable {
        return this.watchableScript.execute("fetch", Collection.class, username, LOGGER);
    }

    @Override
    public void remove(final String username) throws Throwable {
        this.watchableScript.execute("remove", Void.class, username, LOGGER);
    }

    @Override
    public void removeAll() throws Throwable {
        this.watchableScript.execute("removeAll", Void.class, LOGGER);
    }
}
