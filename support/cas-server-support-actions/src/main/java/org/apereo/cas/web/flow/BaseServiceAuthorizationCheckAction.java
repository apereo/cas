package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseServiceAuthorizationCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseServiceAuthorizationCheckAction extends AbstractAction {
    private final ServicesManager servicesManager;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Override
    public Event doExecute(final RequestContext context) {
        val serviceInContext = WebUtils.getService(context);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(serviceInContext);
        if (service == null) {
            return success();
        }

        if (this.servicesManager.getAllServices().isEmpty()) {
            val msg = String.format("No service definitions are found in the service manager. "
                + "Service [%s] will not be automatically authorized to request authentication.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_EMPTY_SVC_MGMR, msg);
        }
        val registeredService = this.servicesManager.findServiceBy(service);

        if (registeredService == null) {
            val msg = String.format("Service [%s] is not found in service registry.", service.getId());
            LOGGER.warn(msg);
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            val msg = String.format("Service Management: Unauthorized Service Access. "
                + "Service [%s] is not allowed access via the service registry.", service.getId());
            LOGGER.warn(msg);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context,
                registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, msg);
        }
        val delegatedPolicy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
        WebUtils.putCasLoginFormViewable(context, delegatedPolicy == null || !delegatedPolicy.isExclusive());
        return success();
    }
}
