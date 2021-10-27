package org.jasig.cas.support.pac4j.authentication;

import org.jasig.cas.authentication.AuthenticationBuilder;
import org.jasig.cas.authentication.AuthenticationMetaDataPopulator;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.ClientCredential;
import org.springframework.stereotype.Component;

/**
 * This class is a meta data populator for authentication. The client name associated to the authentication is added
 * to the authentication attributes.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Component("clientAuthenticationMetaDataPopulator")
public final class ClientAuthenticationMetaDataPopulator implements AuthenticationMetaDataPopulator {

    /***
     * The name of the client used to perform the authentication.
     */
    public static final String CLIENT_NAME = "clientName";

    /**
     * {@inheritDoc}
     */
    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final Credential credential) {
        final ClientCredential clientCredential = (ClientCredential) credential;
        builder.addAttribute(CLIENT_NAME, clientCredential.getCredentials().getClientName());
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof ClientCredential;
    }
}
