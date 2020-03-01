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
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyPasswordHistoryService(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean exists(final PasswordChangeRequest changeRequest) {
        return this.watchableScript.execute("exists", Boolean.class, new Object[]{changeRequest, LOGGER});
    }

    @Override
    public boolean store(final PasswordChangeRequest changeRequest) {
        return this.watchableScript.execute("store", Boolean.class, new Object[]{changeRequest, LOGGER});
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetchAll() {
        return this.watchableScript.execute("fetchAll", Collection.class, new Object[]{LOGGER});
    }

    @Override
    public Collection<? extends PasswordHistoryEntity> fetch(final String username) {
        return this.watchableScript.execute("fetch", Collection.class, new Object[]{username, LOGGER});
    }

    @Override
    public void remove(final String username) {
        this.watchableScript.execute("remove", Void.class, new Object[]{username, LOGGER});
    }

    @Override
    public void removeAll() {
        this.watchableScript.execute("removeAll", Void.class, new Object[]{LOGGER});
    }
}
