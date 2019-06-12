package org.apereo.cas.impl.notify;

import org.apereo.cas.api.AuthenticationRiskNotifier;
import org.apereo.cas.api.AuthenticationRiskScore;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * This is {@link BaseAuthenticationRiskNotifier}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Setter
@Getter
@RequiredArgsConstructor
public abstract class BaseAuthenticationRiskNotifier implements AuthenticationRiskNotifier {

    /**
     * CAS properties.
     */
    protected final CasConfigurationProperties casProperties;

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
