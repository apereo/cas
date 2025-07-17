package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcRegisteredServiceUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcRegisteredServiceUIAction extends BaseCasWebflowAction {

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val serviceCtx = WebUtils.getService(requestContext);
        if (serviceCtx != null) {
            val service = serviceSelectionStrategy.resolveServiceFrom(serviceCtx);
            val registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            LOGGER.debug("Found registered service [{}] from the context", registeredService.getServiceId());
            if (registeredService instanceof final OidcRegisteredService oauthService) {
                WebUtils.putServiceUserInterfaceMetadata(requestContext, new DefaultRegisteredServiceUserInterfaceInfo(oauthService));
            }
        }
        return success();
    }
}
