package org.apereo.cas.web.flow;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSingleSignOnParticipationStrategy.class);
    private boolean createSsoSessionCookieOnRenewAuthentications = true;
    private final ServicesManager servicesManager;

    public DefaultSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                    final boolean createSsoOnRenewedAuthn) {
        this.servicesManager = servicesManager;
        this.createSsoSessionCookieOnRenewAuthentications = createSsoOnRenewedAuthn;
    }

    @Override
    public boolean isParticipating(final RequestContext ctx) {
        
        if (ctx.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.", CasProtocolConstants.PARAMETER_RENEW);
            return this.createSsoSessionCookieOnRenewAuthentications;
        }

        final Authentication authentication = WebUtils.getAuthentication(ctx);
        final Service service = WebUtils.getService(ctx);
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService != null) {
                final Authentication ca = AuthenticationCredentialsLocalBinder.getCurrentAuthentication();
                try {
                    AuthenticationCredentialsLocalBinder.bindCurrent(authentication);
                    final boolean isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso();
                    LOGGER.debug("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                            registeredService.getServiceId(), isAllowedForSso);
                    return isAllowedForSso;
                } finally {
                    AuthenticationCredentialsLocalBinder.bindCurrent(ca);
                }
            }
        }

        return true;
    }
}
