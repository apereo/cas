package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitializeCaptchaAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class InitializeCaptchaAction extends AbstractAction {
    /**
     * The Google recaptcha properties.
     */
    protected final GoogleRecaptchaProperties googleRecaptchaProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        if (googleRecaptchaProperties.isEnabled()) {
            WebUtils.putRecaptchaPropertiesFlowScope(requestContext, googleRecaptchaProperties);
        }
        return new EventFactorySupport().success(this);
    }
}
