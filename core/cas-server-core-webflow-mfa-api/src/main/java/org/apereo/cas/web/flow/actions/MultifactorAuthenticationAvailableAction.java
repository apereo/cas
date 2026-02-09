package org.apereo.cas.web.flow.actions;

import module java.base;
import org.apereo.cas.configuration.model.support.mfa.BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action that will be called as part of the MFA subflow to determine if a MFA provider
 * is up and available to provide authentications.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@RequiredArgsConstructor
public class MultifactorAuthenticationAvailableAction extends AbstractMultifactorAuthenticationAction {
    protected final TenantExtractor tenantExtractor;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val provider = resolveMultifactorAuthenticationProvider(requestContext);
        val failureEval = provider.getFailureModeEvaluator();
        val checkAvailability = Optional.ofNullable(failureEval)
            .map(eval -> eval.evaluate(registeredService, provider) != MultifactorAuthenticationProviderFailureModes.NONE)
            .orElse(true);
        return !checkAvailability || provider.isAvailable(registeredService) ? yes() : no();
    }
}
