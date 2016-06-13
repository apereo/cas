package org.apereo.cas.adaptors.radius.authentication;

import org.apereo.cas.adaptors.radius.web.flow.RadiusMultifactorWebflowConfigurer;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * The authentication provider for yubikey.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RadiusMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Autowired
    private CasConfigurationProperties casProperties;
    
    private RadiusTokenAuthenticationHandler radiusAuthenticationHandler;

    @Override
    public String getId() {
        return RadiusMultifactorWebflowConfigurer.MFA_RADIUS_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return casProperties.getAuthn().getMfa().getRadius().getRank();
    }

    @Override
    protected boolean isAvailable() {
        return this.radiusAuthenticationHandler.canPing();
    }

    public void setRadiusAuthenticationHandler(final RadiusTokenAuthenticationHandler radiusAuthenticationHandler) {
        this.radiusAuthenticationHandler = radiusAuthenticationHandler;
    }
}
