package org.apereo.cas.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskEvaluator;
import org.apereo.cas.api.AuthenticationRiskMitigator;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskBasedAuthenticationEvaluationStartedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskBasedAuthenticationMitigationStartedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationDetectedEvent;
import org.apereo.cas.support.events.authentication.adaptive.CasRiskyAuthenticationMitigatedEvent;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
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
public class RiskAwareAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RiskAwareAuthenticationWebflowEventResolver.class);
    
    private final AuthenticationRiskEvaluator authenticationRiskEvaluator;
    private final AuthenticationRiskMitigator authenticationRiskMitigator;
    private final double threshold;

    public RiskAwareAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                       final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                                       final TicketRegistrySupport ticketRegistrySupport, final CookieGenerator warnCookieGenerator,
                                                       final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                       final MultifactorAuthenticationProviderSelector selector,
                                                       final AuthenticationRiskEvaluator authenticationRiskEvaluator,
                                                       final AuthenticationRiskMitigator authenticationRiskMitigator,
                                                       final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        this.authenticationRiskEvaluator = authenticationRiskEvaluator;
        this.authenticationRiskMitigator = authenticationRiskMitigator;
        threshold = casProperties.getAuthn().getAdaptive().getRisk().getThreshold();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

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

        this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationEvaluationStartedEvent(this, authentication, service));
        
        LOGGER.debug("Evaluating possible suspicious authentication attempt for [{}]", authentication.getPrincipal());
        final AuthenticationRiskScore score = authenticationRiskEvaluator.eval(authentication, service, request);

        if (score.isRiskGreaterThan(threshold)) {
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationDetectedEvent(this, authentication, service, score));

            LOGGER.debug("Calculated risk score [{}] for authentication request by [{}] is above the risk threshold [{}].",
                    score.getScore(),
                    authentication.getPrincipal(),
                    threshold);

            this.eventPublisher.publishEvent(new CasRiskBasedAuthenticationMitigationStartedEvent(this, authentication, service, score));
            final AuthenticationRiskContingencyResponse res = authenticationRiskMitigator.mitigate(authentication, service, score, request);
            this.eventPublisher.publishEvent(new CasRiskyAuthenticationMitigatedEvent(this, authentication, service, res));
            
            return CollectionUtils.wrapSet(res.getResult());
        }

        LOGGER.debug("Authentication request for [{}] is below the risk threshold", authentication.getPrincipal());
        return null;
    }
}
