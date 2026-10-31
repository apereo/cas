package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.logout.slo.SingleLogoutServiceLogoutUrlBuilder;
import org.apereo.cas.services.CasRegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link DefaultLogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultLogoutRedirectionStrategy implements LogoutRedirectionStrategy {
    private final ArgumentExtractor argumentExtractor;

    private final CasConfigurationProperties casProperties;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final ServiceFactory<WebApplicationService> serviceFactory;

    private final ServicesManager servicesManager;

    @Override
    public LogoutRedirectionResponse handle(final HttpServletRequest request, final HttpServletResponse response) {
        val logoutResponse = LogoutRedirectionResponse.builder();
        val redirectUrlFromRequest = WebUtils.getLogoutRedirectUrl(request, String.class);
        if (StringUtils.isNotBlank(redirectUrlFromRequest)) {
            logoutResponse.logoutRedirectUrl(Optional.of(redirectUrlFromRequest));
        } else if (casProperties.getLogout().isFollowServiceRedirects()) {
            val redirectUrl = casProperties.getView().getDefaultRedirectUrl();
            Optional.ofNullable(argumentExtractor.extractService(request))
                .or(() -> FunctionUtils.doIf(StringUtils.isNotBlank(redirectUrl),
                    () -> Optional.ofNullable(serviceFactory.createService(redirectUrl)),
                    Optional::<WebApplicationService>empty).get())
                .filter(service -> isServiceAuthorizedForRedirect(service, request))
                .ifPresent(service -> {
                    LOGGER.debug("Redirecting to logout URL identified by service [{}]", service);
                    logoutResponse.service(Optional.of(service));
                    logoutResponse.logoutRedirectUrl(Optional.of(service.getOriginalUrl()));
                });
        } else {
            LOGGER.debug("Following logout redirects is disabled in CAS settings; the requested service is not looked up");
        }
        return logoutResponse.build();
    }

    /**
     * Whether the logout request may redirect to the service. The service is looked up once and the single logout
     * URL builders are asked whether they support it, rather than through their {@code isServiceAuthorized},
     * which looks the service up again in every builder.
     *
     * @param service the service
     * @param request the request
     * @return true if the service is a registered CAS service the builders support
     */
    protected boolean isServiceAuthorizedForRedirect(final WebApplicationService service, final HttpServletRequest request) {
        val registeredService = servicesManager.findServiceBy(service);
        val authorized = registeredService instanceof final CasRegisteredService casRegisteredService
            && singleLogoutServiceLogoutUrlBuilder.supports(casRegisteredService, service, Optional.of(request));
        if (!authorized) {
            LOGGER.debug("Cannot redirect to [{}] given the service is unauthorized to use CAS. "
                + "Ensure the service is registered with CAS and is enabled to allow access", service);
        }
        return authorized;
    }
}
