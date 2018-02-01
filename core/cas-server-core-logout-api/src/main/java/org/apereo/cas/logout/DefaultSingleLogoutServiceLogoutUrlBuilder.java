package org.apereo.cas.logout;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.UrlValidator;

import java.net.URL;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultSingleLogoutServiceLogoutUrlBuilder implements SingleLogoutServiceLogoutUrlBuilder {
    private final UrlValidator urlValidator;

    @Override
    @SneakyThrows
    public URL determineLogoutUrl(final RegisteredService registeredService, final WebApplicationService singleLogoutService) {

        final URL serviceLogoutUrl = registeredService.getLogoutUrl();
        if (serviceLogoutUrl != null) {
            LOGGER.debug("Logout request will be sent to [{}] for service [{}]", serviceLogoutUrl, singleLogoutService);
            return serviceLogoutUrl;
        }
        final String originalUrl = singleLogoutService.getOriginalUrl();
        if (this.urlValidator.isValid(originalUrl)) {
            LOGGER.debug("Logout request will be sent to [{}] for service [{}]", originalUrl, singleLogoutService);
            return new URL(originalUrl);
        }
        LOGGER.debug("Logout request will not be sent, the URL [{}] for service [{}] is not valid", originalUrl, singleLogoutService);
        return null;
    }
}
