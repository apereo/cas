package org.apereo.cas.web.flow.decorator;

import module java.base;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyLoginWebflowDecorator}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyLoginWebflowDecorator implements WebflowDecorator {
    private final ExecutableCompiledScript watchableScript;

    public GroovyLoginWebflowDecorator(final Resource groovyScript) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyScript);
    }

    @Override
    public void decorate(final RequestContext requestContext) throws Throwable {
        val args = new Object[]{requestContext, LOGGER};
        watchableScript.execute(args, Void.class);
    }
}
