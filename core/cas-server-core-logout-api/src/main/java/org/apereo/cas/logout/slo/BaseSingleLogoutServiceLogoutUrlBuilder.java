package org.apereo.cas.logout.slo;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.UrlValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link BaseSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
@Getter
public abstract class BaseSingleLogoutServiceLogoutUrlBuilder implements SingleLogoutServiceLogoutUrlBuilder {
    /**
     * Services manager instance.
     */
    protected final ServicesManager servicesManager;

    /**
     * The Url validator.
     */
    protected final UrlValidator urlValidator;

    @Override
    public Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                          final WebApplicationService singleLogoutService,
                                                          final Optional<HttpServletRequest> httpRequest) {
        val originalUrl = singleLogoutService.getOriginalUrl();
        if (registeredService instanceof final WebBasedRegisteredService webRegisteredService) {
            val serviceLogoutUrl = webRegisteredService.getLogoutUrl();
            if (StringUtils.hasText(serviceLogoutUrl)) {
                LOGGER.debug("Logout request will be sent to [{}] for service [{}]", serviceLogoutUrl, singleLogoutService);
                return SingleLogoutUrl.from(registeredService);
            }
            if (urlValidator.isValid(originalUrl)) {
                LOGGER.debug("Logout request will be sent to original URL [{}] for service [{}]", originalUrl, singleLogoutService);
                return CollectionUtils.wrap(new SingleLogoutUrl(originalUrl, webRegisteredService.getLogoutType()));
            }
        }
        LOGGER.debug("Logout request will not be sent; The URL [{}] for service [{}] is not valid", originalUrl, singleLogoutService);
        return new ArrayList<>();
    }

    @Override
    public boolean supports(@Nullable
                            final RegisteredService registeredService,
                            @Nullable
                            final WebApplicationService singleLogoutService,
                            final Optional<HttpServletRequest> httpRequest) {
        return registeredService != null && singleLogoutService != null
               && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, singleLogoutService);
    }

    @Override
    public boolean isServiceAuthorized(final WebApplicationService service,
                                       final Optional<HttpServletRequest> request,
                                       final Optional<HttpServletResponse> response) {
        val registeredService = servicesManager.findServiceBy(service);
        return supports(registeredService, service, request);
    }
}
