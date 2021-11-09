package org.apereo.cas.consent;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultConsentActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultConsentActivationStrategy implements ConsentActivationStrategy {
    private final ConsentEngine consentEngine;

    private final CasConfigurationProperties casProperties;

    @Override
    public boolean isConsentRequired(final Service service, final RegisteredService registeredService,
                                     final Authentication authentication,
                                     final HttpServletRequest requestContext) {
        val consentPolicy = registeredService.getAttributeReleasePolicy().getConsentPolicy();
        if (consentPolicy != null) {
            switch (consentPolicy.getStatus()) {
                case TRUE:
                    LOGGER.trace("Attribute consent is enabled for registered service [{}]", registeredService.getName());
                    return consentEngine.isConsentRequiredFor(service, registeredService, authentication).isRequired();
                case FALSE:
                    LOGGER.trace("Attribute consent will be skipped as the attribute consent policy for service [{}] "
                                 + "is disabled for this request", registeredService.getName());
                    return false;
                case UNDEFINED:
                default:
                    LOGGER.trace("Attribute consent policy for service [{}] is undefined", registeredService.getName());
            }
        }
        if (casProperties.getConsent().getCore().isActive()) {
            LOGGER.trace("Attribute consent is enabled globally for all requests");
            return consentEngine.isConsentRequiredFor(service, registeredService, authentication).isRequired();
        }
        LOGGER.trace("Attribute consent will be skipped as neither the attribute consent policy for service [{}] "
                     + "nor the global CAS consent policy are enabled for this request", registeredService.getName());
        return false;
    }
}
