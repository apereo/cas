package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.captcha.GoogleRecaptchaProperties;
import org.apereo.cas.web.CaptchaActivationStrategy;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.function.Consumer;

/**
 * This is {@link InitializeCaptchaAction}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class InitializeCaptchaAction extends BaseCasWebflowAction {
    private final CaptchaActivationStrategy activationStrategy;

    private final Consumer<RequestContext> onActivationConsumer;

    private final GoogleRecaptchaProperties recaptchaProperties;
    
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        activationStrategy.shouldActivate(requestContext, recaptchaProperties)
            .ifPresent(properties -> {
                WebUtils.putRecaptchaPropertiesFlowScope(requestContext, properties);
                onActivationConsumer.accept(requestContext);
            });
        return eventFactory.success(this);
    }
}
