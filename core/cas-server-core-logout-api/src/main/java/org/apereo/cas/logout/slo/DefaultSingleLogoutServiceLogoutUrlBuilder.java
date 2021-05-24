package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;

import javax.servlet.http.HttpServletRequest;
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
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return super.supports(registeredService, singleLogoutService, httpRequest)
            && registeredService.getFriendlyName().equalsIgnoreCase(RegexRegisteredService.FRIENDLY_NAME);
    }
    
}
