package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.service.CasRegisteredServicesLoadedEvent;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link DefaultOAuth20ClientSecretExpirationListener}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultOAuth20ClientSecretExpirationListener implements OAuth20ClientSecretExpirationListener {
    private final ServicesManager servicesManager;

    private final OAuth20ClientSecretValidator clientSecretValidator;

    @Override
    public void handleRegisteredServicesLoadedEvent(final CasRegisteredServicesLoadedEvent event) {
        event.getServices()
            .stream()
            .filter(OAuthRegisteredService.class::isInstance)
            .map(OAuthRegisteredService.class::cast)
            .filter(clientSecretValidator::removeExpiredClientSecrets)
            .forEach(registeredService -> {
                LOGGER.info("Removing expired client secrets from registered service [{}]", registeredService.getName());
                servicesManager.save(registeredService);
            });
    }
}
