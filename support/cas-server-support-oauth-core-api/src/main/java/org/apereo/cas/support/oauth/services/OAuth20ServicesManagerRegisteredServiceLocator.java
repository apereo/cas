package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OAuth20ServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    /**
     * Cas Settings.
     */
    protected final CasConfigurationProperties casProperties;
    
    public OAuth20ServicesManagerRegisteredServiceLocator(final CasConfigurationProperties casProperties) {
        this.casProperties = casProperties;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> {
            var match = supports(registeredService, service);
            if (match) {
                val oauthService = (OAuthRegisteredService) registeredService;
                LOGGER.trace("Attempting to locate service [{}] via [{}]", service, oauthService);
                match = CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.CLIENT_ID))
                    .map(Object::toString)
                    .stream()
                    .anyMatch(clientId -> oauthService.getClientId().equalsIgnoreCase(clientId));
            }
            return match;
        });
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return registeredService instanceof OAuthRegisteredService && supportsInternal(registeredService, service);
    }

    /**
     * Supports internal boolean.
     *
     * @param registeredService the registered service
     * @param givenService      the given service
     * @return the boolean
     */
    protected boolean supportsInternal(final RegisteredService registeredService, final Service givenService) {
        val attributes = givenService.getAttributes();
        if (attributes.containsKey(OAuth20Constants.CLIENT_ID)) {
            val service = (WebApplicationService) givenService;
            val source = CollectionUtils.firstElement(attributes.get(service.getSource()))
                .map(String.class::cast)
                .orElse(StringUtils.EMPTY);
            val callbackService = OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix());
            return StringUtils.isBlank(source) || StringUtils.startsWith(source, callbackService);
        }
        return false;
    }
}

