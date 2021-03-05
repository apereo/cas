package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link BaseAuthenticationRiskContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseAuthenticationRiskContingencyPlan implements AuthenticationRiskContingencyPlan {

    /**
     * CAS properties.
     */
    protected final CasConfigurationProperties casProperties;
    
    /**
     * App context.
     */
    protected final ApplicationContext applicationContext;

    private final Set<AuthenticationRiskNotifier> notifiers = new LinkedHashSet<>(0);

    @Override
    public final AuthenticationRiskContingencyResponse execute(final Authentication authentication,
                                                               final RegisteredService service,
                                                               final AuthenticationRiskScore score,
                                                               final HttpServletRequest request) {
        LOGGER.debug("Executing [{}] to produce a risk response", getClass().getSimpleName());

        notifiers.forEach(e -> {
            e.setAuthentication(authentication);
            e.setAuthenticationRiskScore(score);
            e.setRegisteredService(service);
            LOGGER.debug("Executing risk notification [{}]", e.getClass().getSimpleName());
            new Thread(e, e.getClass().getSimpleName()).start();
        });
        return executeInternal(authentication, service, score, request);
    }

    public Set<AuthenticationRiskNotifier> getNotifiers() {
        return notifiers;
    }

    /**
     * Execute authentication risk contingency plan.
     *
     * @param authentication the authentication
     * @param service        the service
     * @param score          the score
     * @param request        the request
     * @return the authentication risk contingency response. May be null.
     */
    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        return null;
    }

}
