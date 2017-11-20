package org.apereo.cas.authentication.metadata;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;

/**
 * This is {@link AuthenticationCredentialTypeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class AuthenticationCredentialTypeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        builder.mergeAttribute(Credential.CREDENTIAL_TYPE_ATTRIBUTE, transaction.getCredential().getClass().getSimpleName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null;
    }
}
