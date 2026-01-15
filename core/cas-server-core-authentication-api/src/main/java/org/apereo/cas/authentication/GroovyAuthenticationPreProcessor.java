package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyAuthenticationPreProcessor}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
@Setter
@Slf4j
public class GroovyAuthenticationPreProcessor implements AuthenticationPreProcessor, DisposableBean {
    private final ExecutableCompiledScript watchableScript;

    private int order;

    public GroovyAuthenticationPreProcessor(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public boolean process(final AuthenticationTransaction transaction) throws Throwable {
        val args = new Object[]{transaction, LOGGER};
        return Boolean.TRUE.equals(watchableScript.execute(args, Boolean.class));
    }

    @Override
    public boolean supports(final Credential credential) throws Throwable {
        val args = new Object[]{credential, LOGGER};
        return Boolean.TRUE.equals(watchableScript.execute("supports", Boolean.class, args));
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
