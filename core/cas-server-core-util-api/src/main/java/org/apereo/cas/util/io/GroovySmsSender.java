package org.apereo.cas.util.io;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovySmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
public class GroovySmsSender implements SmsSender, DisposableBean {
    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovySmsSender(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
    }

    @Override
    public boolean send(final String from, final String to, final String message) {
        return watchableScript.execute(new Object[]{from, to, message, LOGGER}, Boolean.class);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
