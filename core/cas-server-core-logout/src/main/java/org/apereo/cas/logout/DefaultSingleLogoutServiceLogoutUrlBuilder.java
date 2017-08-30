package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultSingleLogoutServiceLogoutUrlBuilder implements SingleLogoutServiceLogoutUrlBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSingleLogoutServiceLogoutUrlBuilder.class);

    private final UrlValidator urlValidator; 

    public DefaultSingleLogoutServiceLogoutUrlBuilder(final UrlValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    @Override
    public URL determineLogoutUrl(final RegisteredService registeredService, final WebApplicationService singleLogoutService) {
        try {
            final URL serviceLogoutUrl = registeredService.getLogoutUrl();
            if (serviceLogoutUrl != null) {
                LOGGER.debug("Logout request will be sent to [{}] for service [{}]", serviceLogoutUrl, singleLogoutService);
                return serviceLogoutUrl;
            }
            final String originalUrl = singleLogoutService.getOriginalUrl();
            if (this.urlValidator.isValid(originalUrl)) {
                LOGGER.debug("Logout request will be sent to [{}] for service [{}]", originalUrl, singleLogoutService);
                return new URL(originalUrl);
            } else {
                LOGGER.debug("Logout request will not be sent, the URL [{}] for service [{}] is not valid", originalUrl, singleLogoutService);
            }
            return null;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
