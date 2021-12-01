package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DefaultCaptchaActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultCaptchaActivationStrategy implements CaptchaActivationStrategy {
    private final ServicesManager servicesManager;

    private static Optional<GoogleRecaptchaProperties> evaluateResult(final boolean result,
                                                                      final GoogleRecaptchaProperties properties) {
        return result ? Optional.of(properties) : Optional.empty();
    }

    @Override
    public Optional<GoogleRecaptchaProperties> shouldActivate(final RequestContext requestContext,
                                                              final GoogleRecaptchaProperties properties) {
        val service = WebUtils.getService(requestContext);
        val registeredService = servicesManager.findServiceBy(service);
        if (RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.isAssignedTo(registeredService)) {
            LOGGER.trace("Checking for activation of captcha defined for service [{}]", registeredService);
            val result = RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.getPropertyBooleanValue(registeredService);
            return evaluateResult(result, properties);
        }

        LOGGER.trace("Checking for activation of captcha defined under site key [{}]", properties.getSiteKey());
        return evaluateResult(properties.isEnabled(), properties);
    }
}
