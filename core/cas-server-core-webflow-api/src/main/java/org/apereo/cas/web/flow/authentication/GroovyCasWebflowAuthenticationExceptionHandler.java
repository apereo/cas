package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyCasWebflowAuthenticationExceptionHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class GroovyCasWebflowAuthenticationExceptionHandler implements CasWebflowExceptionHandler<Exception> {

    private final ExecutableCompiledScript watchableScript;

    private final ApplicationContext applicationContext;

    private int order = Integer.MIN_VALUE;

    public GroovyCasWebflowAuthenticationExceptionHandler(final Resource groovyScript, final ApplicationContext applicationContext) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyScript);
        this.applicationContext = applicationContext;
    }


    @Override
    public Event handle(final Exception exception, final RequestContext requestContext) throws Throwable {
        val args = new Object[]{exception, requestContext, applicationContext, LOGGER};
        return watchableScript.execute(args, Event.class);
    }

    @Override
    public boolean supports(final Exception exception, final RequestContext requestContext) throws Throwable {
        val args = new Object[]{exception, requestContext, applicationContext, LOGGER};
        return watchableScript.execute("supports", Boolean.class, args);
    }
}
