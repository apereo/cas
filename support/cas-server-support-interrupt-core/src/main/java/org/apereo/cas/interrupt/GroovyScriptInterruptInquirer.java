package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyScriptInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyScriptInterruptInquirer implements InterruptInquirer {
    private final Resource resource;

    public GroovyScriptInterruptInquirer(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public InterruptResponse inquire(final Authentication authentication, final RegisteredService registeredService, final Service service) {
        return null;
    }
}
