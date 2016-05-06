package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * The authentication provider for yubikey.
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("radiusAuthenticationProvider")
public class RadiusMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Value("${cas.mfa.radius.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("radiusTokenAuthenticationHandler")
    private RadiusTokenAuthenticationHandler radiusAuthenticationHandler;

    @Override
    public String getId() {
        return RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }

    @Override
    protected boolean isAvailable() {
        return this.radiusAuthenticationHandler.canPing();
    }
}
