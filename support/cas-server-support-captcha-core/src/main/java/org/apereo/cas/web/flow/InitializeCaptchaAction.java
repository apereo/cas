package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val googleRecaptcha = casProperties.getGoogleRecaptcha();
        WebUtils.putRecaptchaPropertiesFlowScope(requestContext, googleRecaptcha);
        return new EventFactorySupport().success(this);
    }
}
