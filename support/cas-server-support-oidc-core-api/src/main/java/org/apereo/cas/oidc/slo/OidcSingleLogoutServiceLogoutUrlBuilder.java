package org.apereo.cas.oidc.slo;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.slo.BaseSingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.UrlValidator;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OidcSingleLogoutServiceLogoutUrlBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    public OidcSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                   final UrlValidator urlValidator) {
        super(servicesManager, urlValidator);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean supports(final RegisteredService registeredService,
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return super.supports(registeredService, singleLogoutService, httpRequest)
               && registeredService instanceof OidcRegisteredService;
    }
}
