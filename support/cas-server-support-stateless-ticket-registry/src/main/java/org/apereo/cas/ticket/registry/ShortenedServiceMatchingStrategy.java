package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.DefaultServiceMatchingStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * This is {@link ShortenedServiceMatchingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class ShortenedServiceMatchingStrategy extends DefaultServiceMatchingStrategy {
    public ShortenedServiceMatchingStrategy(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    protected boolean compareServices(final Service service, final Service serviceToMatch) throws Exception {
        val originalService = service.getShortenedId();
        val matchingService = serviceToMatch.getShortenedId();
        LOGGER.trace("Decoded urls and comparing [{}] with [{}]", originalService, matchingService);
        return originalService.equalsIgnoreCase(matchingService);
    }
}
