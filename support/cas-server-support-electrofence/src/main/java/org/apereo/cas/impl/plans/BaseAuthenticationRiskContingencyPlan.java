package org.apereo.cas.impl.plans;

import com.google.common.collect.Sets;
import org.apereo.cas.api.AuthenticationRiskContingencyPlan;
import org.apereo.cas.api.AuthenticationRiskContingencyResponse;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * This is {@link BaseAuthenticationRiskContingencyPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRiskContingencyPlan implements AuthenticationRiskContingencyPlan {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * CAS properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;

    /**
     * App context.
     */
    @Autowired
    protected ApplicationContext applicationContext;

    private Set<AuthenticationRiskNotifier> notifiers = Sets.newLinkedHashSet();


    @Override
    public final AuthenticationRiskContingencyResponse execute(final Authentication authentication,
                                                               final RegisteredService service,
                                                               final AuthenticationRiskScore score,
                                                               final HttpServletRequest request) {
        logger.debug("Executing {} to produce a risk response", getClass().getSimpleName());

        notifiers.forEach(e -> e.notify(authentication, service, score));
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
