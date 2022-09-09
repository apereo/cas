package org.apereo.cas.authentication.principal;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * This is {@link DefaultServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
public record DefaultServiceMatchingStrategy(ServicesManager servicesManager) implements ServiceMatchingStrategy {
    @Override
    public boolean matches(final Service service, final Service serviceToMatch) {
        try {
            val thisUrl = URLDecoder.decode(service.getId(), StandardCharsets.UTF_8.name());
            val serviceUrl = URLDecoder.decode(serviceToMatch.getId(), StandardCharsets.UTF_8.name());

            LOGGER.trace("Decoded urls and comparing [{}] with [{}]", thisUrl, serviceUrl);
            return thisUrl.equalsIgnoreCase(serviceUrl);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }
}
