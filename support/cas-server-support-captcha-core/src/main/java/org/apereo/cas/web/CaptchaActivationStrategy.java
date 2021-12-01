package org.apereo.cas.web;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;

import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link CaptchaActivationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface CaptchaActivationStrategy {

    /**
     * Should activate captcha.
     *
     * @param requestContext            the request context
     * @param googleRecaptchaProperties the google recaptcha properties
     * @return the optional
     */
    Optional<GoogleRecaptchaProperties> shouldActivate(RequestContext requestContext,
                                                       GoogleRecaptchaProperties googleRecaptchaProperties);
}
