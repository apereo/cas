package org.apereo.cas.ws.idp.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.ws.idp.services.WSFederationRegisteredService;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WSFederationMetadataUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class WSFederationMetadataUIAction extends AbstractAction {
    private final transient ServicesManager servicesManager;
    private final transient AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        Service service = WebUtils.getService(requestContext);
        if (service != null) {
            service = serviceSelectionStrategy.resolveServiceFrom(service);
            final var registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof WSFederationRegisteredService) {
                final var wsfed = WSFederationRegisteredService.class.cast(registeredService);
                WebUtils.putServiceUserInterfaceMetadata(requestContext, new DefaultRegisteredServiceUserInterfaceInfo(wsfed));
            }
        }
        return success();
    }
}
