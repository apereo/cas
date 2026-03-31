package org.apereo.cas.acct.webflow;

import module java.base;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DisplayAccountRegistrationCompletedAction}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@RequiredArgsConstructor
public class DisplayAccountRegistrationCompletedAction extends BaseCasWebflowAction {
    private final ServiceFactory serviceFactory;
    private final ServicesManager servicesManager;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val registrationRequest = AccountRegistrationUtils.getAccountRegistrationRequest(requestContext);
        if (registrationRequest.containsProperty("service")) {
            val service = serviceFactory.createService(Objects.requireNonNull(registrationRequest.getProperty("service", String.class)));
            WebUtils.putServiceIntoFlowScope(requestContext, service);
            val serviceNumericId = Objects.requireNonNull(registrationRequest.getProperty("registeredServiceNumericId", Long.class));
            val registeredService = servicesManager.findServiceBy(serviceNumericId);
            WebUtils.putRegisteredService(requestContext, registeredService);
        }
        return null;
    }
}
