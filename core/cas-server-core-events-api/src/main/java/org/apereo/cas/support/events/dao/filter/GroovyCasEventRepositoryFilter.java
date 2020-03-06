package org.apereo.cas.support.events.dao.filter;

import org.apereo.cas.support.events.CasEventRepositoryFilter;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyCasEventRepositoryFilter}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class GroovyCasEventRepositoryFilter implements CasEventRepositoryFilter, DisposableBean {
    private final WatchableGroovyScriptResource watchableScript;

    public GroovyCasEventRepositoryFilter(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean shouldSaveEvent(final CasEvent event) {
        val args = new Object[]{event, LOGGER};
        return watchableScript.execute("shouldSaveEvent", Boolean.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
