package org.apereo.cas.ws.idp.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WSFederationMetadataUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class WSFederationMetadataUIAction extends BaseCasWebflowAction {
    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val serviceCtx = WebUtils.getService(requestContext);
        if (serviceCtx != null) {
            val service = serviceSelectionStrategy.resolveServiceFrom(serviceCtx);
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof final WSFederationRegisteredService wsfed) {
                WebUtils.putServiceUserInterfaceMetadata(requestContext, new DefaultRegisteredServiceUserInterfaceInfo(wsfed));
            }
        }
        return success();
    }
}
