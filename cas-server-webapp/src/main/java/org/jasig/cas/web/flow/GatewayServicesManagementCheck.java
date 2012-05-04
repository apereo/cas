package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Performs an authorization check for the gateway request if there is no Ticket Granting Ticket.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.4.5
 */
public class GatewayServicesManagementCheck extends AbstractAction {

    @NotNull
    private final ServicesManager servicesManager;

    public GatewayServicesManagementCheck(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {
        final Service service = WebUtils.getService(context);

        final boolean match = this.servicesManager.matchesExistingService(service);

        if (match) {
            return success();
        }

        throw new UnauthorizedServiceException(String.format("Service [%s] is not authorized to use CAS.", service.getId()));
    }
}
