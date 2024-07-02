package org.apereo.cas.authentication;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyAuthenticationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Setter
@Getter
public class GroovyAuthenticationPostProcessor implements AuthenticationPostProcessor {
    private final ExecutableCompiledScript watchableScript;

    private int order;

    public GroovyAuthenticationPostProcessor(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws Throwable {
        val args = new Object[]{builder, transaction, LOGGER};
        watchableScript.execute(args, Void.class);
    }

    @Override
    public boolean supports(final Credential credential) throws Throwable {
        val args = new Object[]{credential, LOGGER};
        return watchableScript.execute("supports", Boolean.class, args);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}

