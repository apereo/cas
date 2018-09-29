package org.apereo.cas.web.flow.mfa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy.FailureModes;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
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
@RequiredArgsConstructor
public class MultifactorAuthenticationFailureAction extends AbstractMultifactorAuthenticationAction {

    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final RegisteredService service = WebUtils.getRegisteredService(requestContext);

        FailureModes failureMode = FailureModes.valueOf(casProperties.getAuthn().getMfa().getGlobalFailureMode());
        LOGGER.debug("Setting failure mode to [{}] based on Global Policy", failureMode);

        if (provider.failureMode() != FailureModes.NOT_SET) {
            LOGGER.debug("Provider failure mode [{}] overriding Global mode [{}]", provider.failureMode(), failureMode);
            failureMode = provider.failureMode();
        }

        if (service != null) {
            final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
            if (policy != null && policy.getFailureMode() != FailureModes.NOT_SET) {
                LOGGER.debug("Service failure mode [{}] overriding current failure mode [{}]", policy.getFailureMode(), failureMode);
                failureMode = policy.getFailureMode();
            }
        }

        LOGGER.debug("Final failure mode has been determined to be [{}]", failureMode);

        if (failureMode == FailureModes.OPEN) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_BYPASS);
        }

        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_UNAVAILABLE);
    }

}
