package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationHandler")
    private AuthenticationHandler yubiKeyAuthenticationHandler;
    
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public String getId() {
        return GoogleAuthenticatorMultifactorWebflowConfigurer.MFA_GAUTH_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return casProperties.getAuthn().getMfa().getGauth().getRank();
    }


    @Override
    protected boolean isAvailable() {
        return true;
    }
}
