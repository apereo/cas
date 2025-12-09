package org.apereo.cas.notifications.sms;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    private final ExecutableCompiledScript watchableScript;

    public GroovySmsSender(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public boolean send(final String from, final String to, final String message) throws Throwable {
        return Boolean.TRUE.equals(watchableScript.execute(new Object[]{from, to, message, LOGGER}, Boolean.class));
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
