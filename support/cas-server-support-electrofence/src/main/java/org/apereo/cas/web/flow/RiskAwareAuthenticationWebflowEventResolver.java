package org.apereo.cas.web.flow;

import module java.base;
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
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import jakarta.servlet.http.HttpServletRequest;

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


    public RiskAwareAuthenticationWebflowEventResolver(final CasWebflowEventResolutionConfigurationContext context,
        final AuthenticationRiskEvaluator authenticationRiskEvaluator,
        final AuthenticationRiskMitigator authenticationRiskMitigator) {
        super(context);
        this.authenticationRiskEvaluator = authenticationRiskEvaluator;
        this.authenticationRiskMitigator = authenticationRiskMitigator;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val service = WebUtils.getRegisteredService(context);
        val authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        return handlePossibleSuspiciousAttempt(request, authentication, service);
    }

    protected Set<Event> handlePossibleSuspiciousAttempt(final HttpServletRequest request, final Authentication authentication,
        final RegisteredService service) throws Throwable {

        val applicationContext = getConfigurationContext().getApplicationContext();
        val clientInfo = ClientInfoHolder.getClientInfo();
        applicationContext.publishEvent(new CasRiskBasedAuthenticationEvaluationStartedEvent(this, authentication, service, clientInfo));

        LOGGER.debug("Evaluating possible suspicious authentication attempt for [{}]", authentication.getPrincipal());
        val score = authenticationRiskEvaluator.evaluate(authentication, service, clientInfo);

        if (authenticationRiskEvaluator.isRiskyAuthenticationScore(score, authentication, service)) {
            applicationContext.publishEvent(new CasRiskyAuthenticationDetectedEvent(this, authentication, service, score, clientInfo));
            LOGGER.debug("Calculated risk score [{}] for authentication request by [{}] is above the risk threshold",
                score.getScore(), authentication.getPrincipal());
            applicationContext.publishEvent(new CasRiskBasedAuthenticationMitigationStartedEvent(this, authentication, service, score, clientInfo));
            val res = authenticationRiskMitigator.mitigate(authentication, service, score, request);
            applicationContext.publishEvent(new CasRiskyAuthenticationMitigatedEvent(this, authentication, service, res, clientInfo));
            return CollectionUtils.wrapSet(res.result());
        }

        LOGGER.debug("Authentication request for [{}] is below the risk threshold", authentication.getPrincipal());
        return null;
    }
}
