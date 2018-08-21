package org.apereo.cas.interrupt;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.util.HashMap;

/**
 * This is {@link GroovyScriptInterruptInquirer}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyScriptInterruptInquirer extends BaseInterruptInquirer {
    private final Resource resource;

    @Override
    public InterruptResponse inquireInternal(final Authentication authentication, final RegisteredService registeredService,
                                             final Service service, final Credential credential) {
        if (ResourceUtils.doesResourceExist(resource)) {
            val principal = authentication.getPrincipal();
            val attributes = new HashMap<String, Object>(principal.getAttributes());
            attributes.putAll(authentication.getAttributes());
            final Object[] args = {principal.getId(), attributes, service != null ? service.getId() : null, LOGGER};
            return ScriptingUtils.executeGroovyScript(resource, args, InterruptResponse.class, true);
        }
        return InterruptResponse.none();
    }
}
