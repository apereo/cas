package org.apereo.cas.authentication.support.password;

import org.apereo.cas.util.function.FunctionUtils;
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

    private final WatchableGroovyScriptResource watchableScript;

    private final ApplicationContext applicationContext;

    public GroovyPasswordEncoder(final Resource groovyScript, final ApplicationContext applicationContext) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean matches(final CharSequence rawPassword, final String encodedPassword) {
        return FunctionUtils.doUnchecked(() -> {
            val args = new Object[]{rawPassword, encodedPassword, LOGGER, this.applicationContext};
            return watchableScript.execute("matches", Boolean.class, args);
        });
    }

    @Override
    protected byte[] encode(final CharSequence rawPassword, final byte[] salt) {
        return FunctionUtils.doUnchecked(() -> {
            val args = new Object[]{rawPassword, salt, LOGGER, this.applicationContext};
            return watchableScript.execute(args, byte[].class);
        });
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
