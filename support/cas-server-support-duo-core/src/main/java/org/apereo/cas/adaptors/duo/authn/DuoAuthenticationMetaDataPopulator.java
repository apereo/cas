package org.apereo.cas.adaptors.duo.authn;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;

/**
 * Implementation of AuthenticationMetadaPopulator to handle multiple Duo instances.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
@ToString(callSuper = true)
@RequiredArgsConstructor
public class DuoAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    private final String authenticationContextAttribute;

    private final DuoAuthenticationHandler authenticationHandler;

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        if (builder.hasAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
            obj -> obj.toString().equals(this.authenticationHandler.getName()))) {
            builder.mergeAttribute(this.authenticationContextAttribute, authenticationHandler.getProviderId());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return this.authenticationHandler.supports(credential);
    }
}
