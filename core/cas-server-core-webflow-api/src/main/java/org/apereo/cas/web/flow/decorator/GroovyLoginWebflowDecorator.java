package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

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
    private final WatchableGroovyScriptResource watchableScript;

    public GroovyLoginWebflowDecorator(final Resource groovyScript) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyScript);
    }

    @Override
    public void decorate(final RequestContext requestContext) throws Throwable {
        val args = new Object[]{requestContext, LOGGER};
        watchableScript.execute(args, Void.class);
    }
}
