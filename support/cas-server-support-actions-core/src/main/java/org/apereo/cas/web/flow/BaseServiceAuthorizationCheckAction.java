package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseServiceAuthorizationCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseServiceAuthorizationCheckAction extends BaseCasWebflowAction {
    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Override
    protected Event doExecuteInternal(final RequestContext context) {
        val serviceInContext = WebUtils.getService(context);
        val service = FunctionUtils.doUnchecked(() -> authenticationRequestServiceSelectionStrategies.resolveService(serviceInContext));
        if (service == null) {
            return success();
        }
        val registeredService = servicesManager.findServiceBy(service);
        if (registeredService == null) {
            val msg = String.format("Service [%s] is not found in service registry.", service.getId());
            LOGGER.warn(msg);
            throw UnauthorizedServiceException.denied(msg);
        }
        if (!registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            val msg = String.format("Service Management: Unauthorized Service Access. "
                + "Service [%s] is not allowed access via the service registry.", service.getId());
            LOGGER.warn(msg);
            WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context,
                registeredService.getAccessStrategy().getUnauthorizedRedirectUrl());
            throw UnauthorizedServiceException.denied(msg);
        }
        val delegatedPolicy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
        WebUtils.putCasLoginFormViewable(context, delegatedPolicy == null || !delegatedPolicy.isExclusive());
        return success();
    }
}
