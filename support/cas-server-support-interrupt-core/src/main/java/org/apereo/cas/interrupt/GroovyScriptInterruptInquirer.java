package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;
import org.springframework.webflow.execution.RequestContext;
import java.util.HashMap;

/**
 * This is {@link GroovyScriptInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyScriptInterruptInquirer extends BaseInterruptInquirer {
    private final ExecutableCompiledScript watchableScript;

    public GroovyScriptInterruptInquirer(final Resource resource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(resource);
    }

    @Override
    public InterruptResponse inquireInternal(final Authentication authentication,
                                             final RegisteredService registeredService,
                                             final Service service,
                                             final Credential credential,
                                             final RequestContext requestContext) throws Throwable {
        val principal = authentication.getPrincipal();
        val attributes = new HashMap<String, Object>(principal.getAttributes());
        attributes.putAll(authentication.getAttributes());
        val args = new Object[]{principal, attributes, service, registeredService, requestContext, LOGGER};
        LOGGER.trace("Invoking Groovy script with attributes=[{}], service=[{}] and default logger",
            attributes, service != null ? service.getId() : "null");
        return watchableScript.execute(args, InterruptResponse.class);
    }
}
