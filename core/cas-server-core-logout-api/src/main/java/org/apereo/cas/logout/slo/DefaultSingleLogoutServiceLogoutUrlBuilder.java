package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.UrlValidator;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultSingleLogoutServiceLogoutUrlBuilder} which acts on a registered
 * service to determine how the logout url endpoint should be decided.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DefaultSingleLogoutServiceLogoutUrlBuilder extends BaseSingleLogoutServiceLogoutUrlBuilder {
    private final UrlValidator urlValidator;

    public DefaultSingleLogoutServiceLogoutUrlBuilder(final ServicesManager servicesManager,
                                                      final UrlValidator urlValidator) {
        super(servicesManager);
        this.urlValidator = urlValidator;
    }

    @Override
    @SneakyThrows
    public Collection<SingleLogoutUrl> determineLogoutUrl(final RegisteredService registeredService,
                                                          final WebApplicationService singleLogoutService,
                                                          final Optional<HttpServletRequest> httpRequest) {
        val serviceLogoutUrl = registeredService.getLogoutUrl();
        if (serviceLogoutUrl != null) {
            LOGGER.debug("Logout request will be sent to [{}] for service [{}]", serviceLogoutUrl, singleLogoutService);

            return Arrays.stream(StringUtils.commaDelimitedListToStringArray(serviceLogoutUrl))
                .map(url -> new SingleLogoutUrl(url, registeredService.getLogoutType()))
                .collect(Collectors.toList());
        }
        val originalUrl = singleLogoutService.getOriginalUrl();
        if (this.urlValidator.isValid(originalUrl)) {
            LOGGER.debug("Logout request will be sent to [{}] for service [{}]", originalUrl, singleLogoutService);
            return CollectionUtils.wrap(new SingleLogoutUrl(originalUrl, registeredService.getLogoutType()));
        }
        LOGGER.debug("Logout request will not be sent; The URL [{}] for service [{}] is not valid", originalUrl, singleLogoutService);
        return new ArrayList<>(0);
    }
}
