package org.apereo.cas.authentication.metadata;

import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.services.MultifactorAuthenticationProvider;

/**
 * This is {@link AuthenticationContextAttributeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@ToString(callSuper = true)
@AllArgsConstructor
public class AuthenticationContextAttributeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    private final String authenticationContextAttribute;

    private final AuthenticationHandler authenticationHandler;

    private final MultifactorAuthenticationProvider provider;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
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
