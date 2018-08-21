package org.apereo.cas.web.flow.decorator;

import org.apereo.cas.util.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
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
    private final transient Resource groovyScript;

    @Override
    public void decorate(final RequestContext requestContext, final ApplicationContext applicationContext) {
        ScriptingUtils.executeGroovyScript(groovyScript, new Object[]{requestContext, applicationContext, LOGGER}, Void.class, true);
    }
}
