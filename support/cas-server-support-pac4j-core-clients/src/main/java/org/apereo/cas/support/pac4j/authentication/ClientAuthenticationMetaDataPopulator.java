package org.apereo.cas.support.pac4j.authentication;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.metadata.BaseAuthenticationMetaDataPopulator;
import org.apereo.cas.authentication.principal.ClientCredential;

/**
 * This class is a meta data populator for authentication. The client name associated to the authentication is added
 * to the authentication attributes.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@ToString(callSuper = true)
public class ClientAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final ClientCredential clientCredential = (ClientCredential) transaction.getCredential();
        builder.addAttribute(ClientCredential.AUTHENTICATION_ATTRIBUTE_CLIENT_NAME, clientCredential.getCredentials().getClientName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof ClientCredential;
    }
}
