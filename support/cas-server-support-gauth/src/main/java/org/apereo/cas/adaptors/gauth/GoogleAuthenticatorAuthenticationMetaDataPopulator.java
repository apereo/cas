package org.apereo.cas.adaptors.gauth;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.MultifactorAuthenticationProvider;

/**
 * This is {@link GoogleAuthenticatorAuthenticationMetaDataPopulator} which inserts the
 * MFA provider id into the final authentication object.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private String authenticationContextAttribute;
    private AuthenticationHandler authenticationHandler;
    private MultifactorAuthenticationProvider provider;

    public GoogleAuthenticatorAuthenticationMetaDataPopulator(final String authenticationContextAttribute, 
                                                              final AuthenticationHandler authenticationHandler, 
                                                              final MultifactorAuthenticationProvider provider) {
        this.authenticationContextAttribute = authenticationContextAttribute;
        this.authenticationHandler = authenticationHandler;
        this.provider = provider;
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        if (builder.hasAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
                obj -> obj.toString().equals(this.authenticationHandler.getName()))) {
            builder.mergeAttribute(this.authenticationContextAttribute, this.provider.getId());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return this.authenticationHandler.supports(credential);
    }
}

