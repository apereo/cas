package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
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
    private final transient ApplicationContext applicationContext;

    public GroovyPasswordEncoder(final Resource groovyScript, final ApplicationContext applicationContext) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
        this.applicationContext = applicationContext;
    }

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        val args = new Object[]{rawPassword, salt, LOGGER, this.applicationContext};
        return watchableScript.execute(args, byte[].class);
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        val args = new Object[]{rawPassword, encodedPassword, LOGGER, this.applicationContext};
        return watchableScript.execute("matches", Boolean.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
