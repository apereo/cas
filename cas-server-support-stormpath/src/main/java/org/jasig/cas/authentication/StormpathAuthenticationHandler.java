package org.jasig.cas.authentication;

import org.jasig.cas.authentication.handler.PasswordEncoder;
import org.jasig.cas.authentication.handler.PrincipalNameTransformer;
import org.jasig.cas.integration.pac4j.authentication.handler.support.UsernamePasswordWrapperAuthenticationHandler;
import org.pac4j.http.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.password.NopPasswordEncoder;
import org.pac4j.stormpath.credentials.authenticator.StormpathAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link StormpathAuthenticationHandler} that verifies accounts
 * against Stormpath Cloud.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("stormpathAuthenticationHandler")
public class StormpathAuthenticationHandler extends UsernamePasswordWrapperAuthenticationHandler {
    @Value("${cas.authn.stormpath.api.key:}")
    private String apiKey;

    @Value("${cas.authn.stormpath.app.id:}")
    private String applicationId;

    @Value("${cas.authn.stormpath.secret.key:}")
    private String secretkey;

    @Autowired(required=false)
    @Qualifier("stormpathPac4jPasswordEncoder")
    private org.pac4j.http.credentials.password.PasswordEncoder stormpathPasswordEncoder = new NopPasswordEncoder();

    @Autowired(required = false)
    @Override
    public void setPasswordEncoder(@Qualifier("stormpathPasswordEncoder")
                                   final PasswordEncoder passwordEncoder) {
        if (passwordEncoder != null) {
            super.setPasswordEncoder(passwordEncoder);
        }
    }

    @Autowired(required=false)
    @Override
    public void setPrincipalNameTransformer(@Qualifier("stormpathPrincipalNameTransformer")
                                            final PrincipalNameTransformer principalNameTransformer) {
        if (principalNameTransformer != null) {
            super.setPrincipalNameTransformer(principalNameTransformer);
        }
    }

    @Override
    protected Authenticator getAuthenticator(final Credential credential) {
        final StormpathAuthenticator authenticator = new StormpathAuthenticator(this.apiKey, this.secretkey, this.applicationId);
        authenticator.setPasswordEncoder(this.stormpathPasswordEncoder);
        return authenticator;
    }
}
