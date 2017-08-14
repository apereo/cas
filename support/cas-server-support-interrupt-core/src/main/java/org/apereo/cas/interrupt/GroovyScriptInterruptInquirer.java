package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link GroovyScriptInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyScriptInterruptInquirer extends BaseInterruptInquirer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyScriptInterruptInquirer.class);

    private final Resource resource;

    public GroovyScriptInterruptInquirer(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        if (ResourceUtils.doesResourceExist(resource)) {
            final Principal principal = authentication.getPrincipal();
            final Map<String, Object> attributes = new LinkedHashMap<>(principal.getAttributes());
            attributes.putAll(authentication.getAttributes());
            final Object[] args = {principal.getId(), attributes, service != null ? service.getId() : null, LOGGER};
            return ScriptingUtils.executeGroovyScript(resource, args, InterruptResponse.class);
        }
        return new InterruptResponse(false);
    }
}
