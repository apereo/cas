package org.apereo.cas.web.flow;

import com.google.common.collect.Sets;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.events.CasRiskBasedAuthenticationEvaluationStartedEvent;
import org.apereo.cas.support.events.CasRiskBasedAuthenticationMitigationStartedEvent;
import org.apereo.cas.support.events.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.CasRiskyAuthenticationMitigatedEvent;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RiskAwareAuthenticationWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Autowired
    private CasConfigurationProperties casProperties;

    private final AuthenticationRiskEvaluator authenticationRiskEvaluator;
    private final AuthenticationRiskMitigator authenticationRiskMitigator;

    public RiskAwareAuthenticationWebflowEventResolver(final AuthenticationRiskEvaluator authenticationRiskEvaluator,
                                                       final AuthenticationRiskMitigator authenticationRiskMitigator) {
        this.authenticationRiskEvaluator = authenticationRiskEvaluator;
        this.authenticationRiskMitigator = authenticationRiskMitigator;
    }

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequest(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
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
    protected Set<Event> handlePossibleSuspiciousAttempt(final HttpServletRequest request,
                                                         final Authentication authentication,
                                                         final RegisteredService service) {

        this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationEvaluationStartedEvent(this, authentication, service));
        
        logger.debug("Evaluating possible suspicious authentication attempt for {}", authentication.getPrincipal());
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);
                
        if (score.getScore() >= casProperties.getAuthn().getAdaptive().getRisk().getThreshold()) {
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationDetectedEvent(this, authentication, service, score));

            logger.debug("Calculated risk score {} for authentication request by {} is above the risk threshold {}.",
                    score.getScore(),
                    authentication.getPrincipal(),
                    casProperties.getAuthn().getAdaptive().getRisk().getThreshold());

            this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationMitigationStartedEvent(this, authentication, service, score));
            final AuthenticationRiskContingencyResponse res = authenticationRiskMitigator.mitigate(authentication, service, score, request);
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationMitigatedEvent(this, authentication, service, res));
            
            return Sets.newHashSet(res.getResult());
        }

        logger.debug("Authentication request for {} is below the risk threshold", authentication.getPrincipal());
        return null;
    }
}
