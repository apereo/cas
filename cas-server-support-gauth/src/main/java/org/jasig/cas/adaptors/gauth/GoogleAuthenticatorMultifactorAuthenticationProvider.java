package org.jasig.cas.adaptors.gauth;

import org.jasig.cas.adaptors.gauth.web.flow.GoogleAuthenticatorMultifactorWebflowConfigurer;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.services.AbstractMultifactorAuthenticationProvider;
import org.jasig.cas.util.http.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * The authentication provider for google authenticator.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("googleAuthenticatorAuthenticationProvider")
public class GoogleAuthenticatorMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 4789727148634156909L;

    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationHandler")
    private AuthenticationHandler yubiKeyAuthenticationHandler;

    @Value("${cas.mfa.gauth.rank:0}")
    private int rank;

    @Autowired
    @Qualifier("noRedirectHttpClient")
    private HttpClient httpClient;


    @Override
    public String getId() {
        return GoogleAuthenticatorMultifactorWebflowConfigurer.MFA_GAUTH_EVENT_ID;
    }

    @Override
    public int getOrder() {
        return this.rank;
    }


    @Override
    protected boolean isAvailable() {
        return true;
    }
}
