package org.apereo.cas.support.pac4j.authentication;

import org.apache.commons.lang3.builder.ToStringBuilder;
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
public class ClientAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    /***
     * The name of the client used to perform the authentication.
     */
    public static final String CLIENT_NAME = "clientName";

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        final ClientCredential clientCredential = (ClientCredential) transaction.getCredential();
        builder.addAttribute(CLIENT_NAME, clientCredential.getCredentials().getClientName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof ClientCredential;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .toString();
    }
}
