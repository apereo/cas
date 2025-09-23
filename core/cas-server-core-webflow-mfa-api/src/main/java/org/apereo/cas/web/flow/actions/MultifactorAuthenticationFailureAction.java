package org.apereo.cas.web.flow.actions;

import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action executed to determine how a MFA provider should fail if unavailable.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@RequiredArgsConstructor
public class MultifactorAuthenticationFailureAction extends AbstractMultifactorAuthenticationAction {
    protected final TenantExtractor tenantExtractor;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        val service = WebUtils.getRegisteredService(requestContext);
        val failureMode = provider.getFailureModeEvaluator().evaluate(service, provider);
        LOGGER.debug("Final failure mode has been determined to be [{}]", failureMode);
        if (failureMode.isAllowedToBypass()) {
            LOGGER.debug("Failure mode [{}] is allowed to bypass multifactor authentication", failureMode);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }

        return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }

}
