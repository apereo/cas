package org.apereo.cas.authentication;

import org.apereo.cas.services.MultifactorAuthenticationProvider;

/**
 * This is {@link AuthenticationContextAttributeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AuthenticationContextAttributeMetaDataPopulator implements AuthenticationMetaDataPopulator {
    private final String authenticationContextAttribute;
    private final AuthenticationHandler authenticationHandler;
    private final MultifactorAuthenticationProvider provider;

    public AuthenticationContextAttributeMetaDataPopulator(final String authenticationContextAttribute,
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
