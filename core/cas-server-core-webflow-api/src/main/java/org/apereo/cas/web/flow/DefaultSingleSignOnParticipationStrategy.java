package org.apereo.cas.web.flow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DefaultSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final ServicesManager servicesManager;
    private final boolean createSsoSessionCookieOnRenewAuthentications;
    private final boolean renewEnabled;

    @Override
    public boolean isParticipating(final RequestContext ctx) {
        if (renewEnabled && ctx.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.",
                CasProtocolConstants.PARAMETER_RENEW);
            return this.createSsoSessionCookieOnRenewAuthentications;
        }

        final var authentication = WebUtils.getAuthentication(ctx);
        final Service service = WebUtils.getService(ctx);
        if (service != null) {
            final var registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService != null) {
                final var ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
                try {
                    AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
                    final var isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso();
                    LOGGER.debug("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                        registeredService.getServiceId(), isAllowedForSso);
                    return isAllowedForSso;
                } finally {
                    AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
                }
            }
        }

        return true;
    }
}
