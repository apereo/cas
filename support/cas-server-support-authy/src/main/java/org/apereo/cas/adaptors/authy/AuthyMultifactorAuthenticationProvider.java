package org.apereo.cas.adaptors.authy;

import org.apereo.cas.adaptors.authy.web.flow.AuthyMultifactorWebflowConfigurer;
import org.apereo.cas.services.AbstractMultifactorAuthenticationProvider;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;
    
    @Override
    public String getId() {
        return AuthyMultifactorWebflowConfigurer.MFA_AUTHY_EVENT_ID;
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
