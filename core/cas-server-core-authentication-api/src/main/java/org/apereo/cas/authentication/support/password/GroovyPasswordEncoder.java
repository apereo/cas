package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;
import org.apereo.cas.util.spring.ApplicationContextProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;
import org.springframework.security.crypto.password.AbstractPasswordEncoder;

/**
 * This is {@link GroovyPasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyPasswordEncoder extends AbstractPasswordEncoder implements DisposableBean {

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyPasswordEncoder(final Resource groovyScript) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        val args = new Object[]{rawPassword, salt, LOGGER, ApplicationContextProvider.getApplicationContext()};
        return watchableScript.execute(args, byte[].class);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
