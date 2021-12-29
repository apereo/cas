package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.RegexUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;
import java.util.Set;

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

            if (RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_IP_ADDRESS_PATTERN.isAssignedTo(registeredService)) {
                val ip = Optional.ofNullable(ClientInfoHolder.getClientInfo())
                    .map(ClientInfo::getClientIpAddress).orElse(StringUtils.EMPTY).trim();
                LOGGER.trace("Checking for activation of captcha defined for service [{}] based on IP address [{}]", registeredService, ip);
                val ipPattern = RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_IP_ADDRESS_PATTERN.getPropertyValues(registeredService, Set.class);
                val result = ipPattern.stream().anyMatch(pattern -> RegexUtils.find(pattern.toString().trim(), ip));
                return evaluateResult(result, properties);
            }

            val result = RegisteredServiceProperty.RegisteredServiceProperties.CAPTCHA_ENABLED.getPropertyBooleanValue(registeredService);
            return evaluateResult(result, properties);
        }

        if (StringUtils.isNotBlank(properties.getActivateForIpAddressPattern())) {
            val ip = Optional.ofNullable(ClientInfoHolder.getClientInfo())
                .map(ClientInfo::getClientIpAddress).orElse(StringUtils.EMPTY);
            LOGGER.debug("Remote IP address [{}] will be checked against [{}]", ip, properties.getActivateForIpAddressPattern());
            val activate = RegexUtils.find(properties.getActivateForIpAddressPattern(), ip);
            return evaluateResult(activate, properties);
        }

        LOGGER.trace("Checking for activation of captcha defined under site key [{}]", properties.getSiteKey());
        return evaluateResult(properties.isEnabled(), properties);
    }
}
