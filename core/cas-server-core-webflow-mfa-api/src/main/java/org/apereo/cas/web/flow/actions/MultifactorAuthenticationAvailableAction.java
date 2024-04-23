package org.apereo.cas.web.flow.actions;

import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * Action that will be called as part of the MFA subflow to determine if a MFA provider
 * is up and available to provide authentications.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
public class MultifactorAuthenticationAvailableAction extends AbstractMultifactorAuthenticationAction {

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val failureEval = provider.getFailureModeEvaluator();
        val checkAvailability = Optional.ofNullable(failureEval)
            .map(eval -> eval.evaluate(registeredService, provider) != MultifactorAuthenticationProviderFailureModes.NONE)
            .orElse(true);
        return !checkAvailability || provider.isAvailable(registeredService) ? yes() : no();
    }
}
