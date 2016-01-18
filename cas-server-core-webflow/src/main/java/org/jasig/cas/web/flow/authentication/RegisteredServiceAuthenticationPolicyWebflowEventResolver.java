package org.jasig.cas.web.flow.authentication;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("registeredServiceAuthenticationPolicyWebflowEventResolver")
public class RegisteredServiceAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        if (service.getAuthenticationPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            return null;
        }
        final Set<Event> event = resolveEventPerAuthenticationProvider(context, service);
        return event;
    }
}
