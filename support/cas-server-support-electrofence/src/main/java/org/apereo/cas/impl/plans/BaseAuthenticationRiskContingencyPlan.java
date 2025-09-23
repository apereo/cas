package org.apereo.cas.impl.plans;

import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ApplicationContext;
import jakarta.servlet.http.HttpServletRequest;
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

    protected final CasConfigurationProperties casProperties;
    
    protected final ApplicationContext applicationContext;

    @Getter
    protected final Set<AuthenticationRiskNotifier> notifiers = new LinkedHashSet<>();

    @Override
    public final AuthenticationRiskContingencyResponse execute(final Authentication authentication,
                                                               final RegisteredService service,
                                                               final AuthenticationRiskScore score,
                                                               final HttpServletRequest request) throws Throwable {
        LOGGER.debug("Executing [{}] to produce a risk response", getClass().getSimpleName());
        for (val notifier : notifiers) {
            notifier.setAuthentication(authentication);
            notifier.setAuthenticationRiskScore(score);
            notifier.setRegisteredService(service);
            notifier.setClientInfo(ClientInfoHolder.getClientInfo());
            LOGGER.debug("Publishing risk notification [{}]", notifier.getClass().getSimpleName());
            notifier.publish();
        }
        return executeInternal(authentication, service, score, request);
    }

    protected AuthenticationRiskContingencyResponse executeInternal(final Authentication authentication,
                                                                    final RegisteredService service,
                                                                    final AuthenticationRiskScore score,
                                                                    final HttpServletRequest request) {
        return null;
    }

}
