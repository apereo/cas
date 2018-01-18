package org.apereo.cas.impl.notify;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.Setter;

/**
 * This is {@link BaseAuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Setter
@Getter
public abstract class BaseAuthenticationRiskNotifier implements AuthenticationRiskNotifier {

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
    protected RegisteredService registeredService;

    /**
     * The Score.
     */
    protected AuthenticationRiskScore authenticationRiskScore;

    @Override
    public void run() {
        publish();
    }
}
