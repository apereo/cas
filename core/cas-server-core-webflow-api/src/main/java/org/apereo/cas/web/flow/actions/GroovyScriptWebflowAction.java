package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyScriptWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class GroovyScriptWebflowAction extends BaseCasWebflowAction {
    private final ExecutableCompiledScript script;

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val applicationContext = requestContext.getActiveFlow().getApplicationContext();
        val args = new Object[]{requestContext, applicationContext, casProperties, LOGGER};
        return script.execute(args, Event.class);
    }
}
