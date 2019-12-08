package org.apereo.cas.web.flow.actions;

import org.apereo.cas.services.RegisteredServiceMultifactorPolicyFailureModes;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Action executed to determine how a MFA provider should fail if unavailable.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
public class MultifactorAuthenticationFailureAction extends AbstractMultifactorAuthenticationAction {
    public MultifactorAuthenticationFailureAction(final ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val service = WebUtils.getRegisteredService(requestContext);
        val failureMode = provider.getFailureModeEvaluator().evaluate(service, provider);

        LOGGER.debug("Final failure mode has been determined to be [{}]", failureMode);

        if (failureMode == RegisteredServiceMultifactorPolicyFailureModes.OPEN) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }

        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }

}
