package org.jasig.cas.adaptors.gauth;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * This is {@link GoogleAuthenticatorAuthenticationMetaDataPopulator} which inserts the
 * MFA provider id into the final authentication object.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("googleAuthenticatorAuthenticationMetaDataPopulator")
public class GoogleAuthenticatorAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;


    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationProvider")
    private MultifactorAuthenticationProvider provider;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        if (builder.hasAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
                obj -> obj.toString().equals(this.authenticationHandler.getName()))) {
            builder.mergeAttribute(authenticationContextAttribute, provider.getId());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return authenticationHandler.supports(credential);
    }
}

