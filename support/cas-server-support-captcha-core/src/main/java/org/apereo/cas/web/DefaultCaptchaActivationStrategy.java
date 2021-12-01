package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Override
    public Optional<GoogleRecaptchaProperties> shouldActivate(final RequestContext requestContext,
                                                              final GoogleRecaptchaProperties properties) {
        LOGGER.trace("Checking for activation of captcha defined under site key [{}]", properties.getSiteKey());
        return properties.isEnabled() ? Optional.of(properties) : Optional.empty();
    }
}
