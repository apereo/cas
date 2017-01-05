package org.apereo.cas.adaptors.radius;

import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.AuthenticationBuilder;

/**
 * This is {@link RadiusAuthenticationMetaDataPopulator} which inserts the
 * radius MFA provider id into the final authentication object.
 *
 * @author Misagh Moayyed
 * @author Nagai Takayuki
 * @since 5.0.0
 */
public class RadiusAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    private final String authenticationContextAttribute;
    private final AuthenticationHandler authenticationHandler;
    private final MultifactorAuthenticationProvider provider;

    public RadiusAuthenticationMetaDataPopulator(final String authenticationContextAttribute, final AuthenticationHandler authenticationHandler,
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
