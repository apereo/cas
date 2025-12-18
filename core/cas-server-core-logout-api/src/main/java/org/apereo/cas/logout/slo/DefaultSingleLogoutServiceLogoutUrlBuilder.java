package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;

import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    public DefaultSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                      final UrlValidator urlValidator) {
        super(servicesManager, urlValidator);
    }

    @Override
    public boolean supports(
        @Nullable
        final RegisteredService registeredService,
        @Nullable
        final WebApplicationService singleLogoutService,
        final Optional<HttpServletRequest> httpRequest) {
        return super.supports(registeredService, singleLogoutService, httpRequest)
            && Objects.requireNonNull(registeredService).getFriendlyName().equalsIgnoreCase(CasRegisteredService.FRIENDLY_NAME);
    }

}
