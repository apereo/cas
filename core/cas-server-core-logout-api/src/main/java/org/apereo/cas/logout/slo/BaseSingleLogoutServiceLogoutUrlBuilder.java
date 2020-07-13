package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * This is {@link BaseSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSingleLogoutServiceLogoutUrlBuilder implements SingleLogoutServiceLogoutUrlBuilder {
    /**
     * Services manager instance.
     */
    protected final ServicesManager servicesManager;

    @Override
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return registeredService != null && singleLogoutService != null
            && registeredService.getAccessStrategy().isServiceAccessAllowed();
    }

    @Override
    public boolean isServiceAuthorized(final WebApplicationService service,
                                       final Optional<HttpServletRequest> request) {
        val registeredService = servicesManager.findServiceBy(service);
        return supports(registeredService, service, request);
    }
}
