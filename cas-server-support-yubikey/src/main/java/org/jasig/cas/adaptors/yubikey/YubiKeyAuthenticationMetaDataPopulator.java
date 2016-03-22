package org.jasig.cas.adaptors.yubikey;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This is {@link YubiKeyAuthenticationMetaDataPopulator} which inserts the
 * yubikey MFA provider id into the final authentication object.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("yubikeyAuthenticationMetaDataPopulator")
public class YubiKeyAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    @Value("${cas.mfa.authn.ctx.attribute:authnContextClass}")
    private String authenticationContextAttribute;

    @Autowired
    @Qualifier("yubikeyAuthenticationHandler")
    private AuthenticationHandler authenticationHandler;


    @Autowired
    @Qualifier("yubikeyAuthenticationProvider")
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

