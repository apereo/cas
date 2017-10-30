package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.services.DefaultRegisteredServiceUserInterfaceInfo;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;

/**
 * This is {@link OidcRegisteredServiceUIAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcRegisteredServiceUIAction extends AbstractAction implements Serializable {

    private static final long serialVersionUID = -8016284160122109307L;
    private final transient ServicesManager servicesManager;

    private final transient AuthenticationServiceSelectionStrategy serviceSelectionStrategy;

    public OidcRegisteredServiceUIAction(final ServicesManager servicesManager,
                                         final AuthenticationServiceSelectionStrategy serviceSelectionStrategy) {
        this.servicesManager = servicesManager;
        this.serviceSelectionStrategy = serviceSelectionStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        Service service = WebUtils.getService(requestContext);
        if (service != null) {
            service = serviceSelectionStrategy.resolveServiceFrom(service);
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);

            if (registeredService instanceof OidcRegisteredService) {
                final OidcRegisteredService oauthService = OidcRegisteredService.class.cast(registeredService);
                WebUtils.putServiceUserInterfaceMetadata(requestContext, new DefaultRegisteredServiceUserInterfaceInfo(oauthService));
            }
        }
        return success();
    }
}
