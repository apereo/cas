package org.apereo.cas.util.io;

import org.apereo.cas.util.scripting.ScriptingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovySmsSender}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class GroovySmsSender implements SmsSender {
    private final transient Resource groovyResource;

    @Override
    public boolean send(final String from, final String to, final String message) {
        return ScriptingUtils.executeGroovyScript(this.groovyResource, new Object[]{from, to, message, LOGGER}, Boolean.class, true);
    }
}
