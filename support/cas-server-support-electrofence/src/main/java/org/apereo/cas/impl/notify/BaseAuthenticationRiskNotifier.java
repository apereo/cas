package org.apereo.cas.impl.notify;

import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link BaseAuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseAuthenticationRiskNotifier implements AuthenticationRiskNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseAuthenticationRiskNotifier.class);
    
    /**
     * Cas properties.
     */
    @Autowired
    protected CasConfigurationProperties casProperties;
    
    /**
     * The Authentication.
     */
    protected Authentication authentication;
    /**
     * The Service.
     */
    protected RegisteredService service;
    /**
     * The Score.
     */
    protected AuthenticationRiskScore score;

    @Override
    public void setAuthentication(final Authentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public void setRegisteredService(final RegisteredService service) {
        this.service = service;
    }

    @Override
    public void setAuthenticationRiskScore(final AuthenticationRiskScore score) {
        this.score = score;
    }

    @Override
    public void run() {
        publish();
    }
}
