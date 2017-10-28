package org.apereo.cas.web.flow.action;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SurrogateAuthorizationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SurrogateAuthorizationAction extends AbstractAction {
    private final ServicesManager servicesManager;
    
    public SurrogateAuthorizationAction(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;   
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication ca = AuthenticationCredentialsLocalBinder.getCurrentAuthentication();
        try {
            final Service service = WebUtils.getService(requestContext);
            final Authentication authentication = WebUtils.getAuthentication(requestContext);
            final RegisteredService svc = WebUtils.getRegisteredService(requestContext);
            if (svc != null) {
                AuthenticationCredentialsLocalBinder.bindCurrent(authentication);
                RegisteredServiceAccessStrategyUtils.ensurePrincipalAccessIsAllowedForService(service, svc, authentication);
                return success();
            }
            return null;
        } finally {
            AuthenticationCredentialsLocalBinder.bindCurrent(ca);
        }
    }
}
