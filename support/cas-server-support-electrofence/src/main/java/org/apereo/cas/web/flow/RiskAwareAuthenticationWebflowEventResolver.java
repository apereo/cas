package org.apereo.cas.web.flow;

import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskBasedAuthenticationEvaluationStartedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskBasedAuthenticationMitigationStartedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationMitigatedEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.CasWebflowEventResolutionConfigurationContext;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;

import java.util.Set;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    private final AuthenticationRiskEvaluator authenticationRiskEvaluator;

    private final AuthenticationRiskMitigator authenticationRiskMitigator;

    private final double threshold;

    public RiskAwareAuthenticationWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext webflowEventResolutionConfigurationContext,
                                                       final AuthenticationRiskEvaluator authenticationRiskEvaluator,
                                                       final AuthenticationRiskMitigator authenticationRiskMitigator) {
        super(webflowEventResolutionConfigurationContext);
        this.authenticationRiskEvaluator = authenticationRiskEvaluator;
        this.authenticationRiskMitigator = authenticationRiskMitigator;
        threshold = webflowEventResolutionConfigurationContext.getCasProperties().getAuthn().getAdaptive().getRisk().getThreshold();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val service = WebUtils.getRegisteredService(context);
        val authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        return handlePossibleSuspiciousAttempt(request, authentication, service);
    }

    /**
     * Handle possible suspicious attempt.
     *
     * @param request        the request
     * @param authentication the authentication
     * @param service        the service
     * @return the set
     */
    protected Set<Event> handlePossibleSuspiciousAttempt(final HttpServletRequest request, final Authentication authentication,
                                                         final RegisteredService service) {

        val applicationContext = getWebflowEventResolutionConfigurationContext().getApplicationContext();
        applicationContext
            .publishEvent(new CasRiskBasedAuthenticationEvaluationStartedEvent(this, authentication, service));

        LOGGER.debug("Evaluating possible suspicious authentication attempt for [{}]", authentication.getPrincipal());
        val score = authenticationRiskEvaluator.eval(authentication, service, request);

        if (score.isRiskGreaterThan(threshold)) {
            applicationContext
                .publishEvent(new CasRiskyAuthenticationDetectedEvent(this, authentication, service, score));

            LOGGER.debug("Calculated risk score [{}] for authentication request by [{}] is above the risk threshold [{}].",
                score.getScore(),
                authentication.getPrincipal(),
                threshold);

            applicationContext
                .publishEvent(new CasRiskBasedAuthenticationMitigationStartedEvent(this, authentication, service, score));
            val res = authenticationRiskMitigator.mitigate(authentication, service, score, request);
            applicationContext
                .publishEvent(new CasRiskyAuthenticationMitigatedEvent(this, authentication, service, res));

            return CollectionUtils.wrapSet(res.getResult());
        }

        LOGGER.debug("Authentication request for [{}] is below the risk threshold", authentication.getPrincipal());
        return null;
    }
}
