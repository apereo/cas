package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Performs an authorization check for the gateway request if there is no Ticket Granting Ticket.
 *
 * @author Scott Battaglia
 * @since 3.4.5
 */
@Slf4j
@AllArgsConstructor
public class GatewayServicesManagementCheck extends AbstractAction {
    private final ServicesManager servicesManager;

    @Override
    protected Event doExecute(final RequestContext context) {
        final Service service = WebUtils.getService(context);

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        if (registeredService == null) {
            final String msg = String.format("Service Management: Unauthorized Service Access. "
                + "Service [%s] does not match entries in service registry.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }

        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            final String msg = String.format("Service Management: Access to service [%s] "
                + "is disabled by the service registry.", service.getId());
            LOGGER.warn(msg);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        return success();
    }
}
