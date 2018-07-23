package org.apereo.cas.support.pac4j.authentication;

import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.ClientCredential;

import lombok.ToString;
import lombok.val;

/**
 * This class is a meta data populator for authentication. The client name associated to the authentication is added
 * to the authentication attributes.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@ToString(callSuper = true)
public class ClientAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        transaction.getPrimaryCredential().ifPresent(clientCredential -> {
            val credentials = ClientCredential.class.cast(clientCredential);
            builder.addAttribute(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, credentials.getClientName());
        });
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof ClientCredential;
    }
}
