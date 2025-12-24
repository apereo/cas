package org.apereo.cas.authentication.metadata;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.jspecify.annotations.Nullable;


/**
 * This is {@link AuthenticationCredentialTypeMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class AuthenticationCredentialTypeMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(credential -> builder.mergeAttribute(Credential.CREDENTIAL_TYPE_ATTRIBUTE, credential.getClass().getSimpleName()));
    }

    @Override
    public boolean supports(@Nullable final Credential credential) {
        return credential != null;
    }
}
