package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.validation.ValidationServiceSelectionStrategy;
import org.apereo.cas.web.flow.services.BaseRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OidcRegisteredServiceUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcRegisteredServiceUIAction extends AbstractAction {
    private final ServicesManager servicesManager;

    private final ValidationServiceSelectionStrategy serviceSelectionStrategy;

    public OidcRegisteredServiceUIAction(final ServicesManager servicesManager,
                                         final ValidationServiceSelectionStrategy serviceSelectionStrategy) {
        this.servicesManager = servicesManager;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        Service service = WebUtils.getService(requestContext);
        if (service != null) {
            service = serviceSelectionStrategy.resolveServiceFrom(service);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof OidcRegisteredService) {
                final OidcRegisteredService oauthService = OidcRegisteredService.class.cast(registeredService);
                WebUtils.putServiceUserInterfaceMetadata(requestContext,
                        new BaseRegisteredServiceUserInterfaceInfo(oauthService) {
                        });
            }
        }
        return success();
    }
}
