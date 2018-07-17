package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.integration.pac4j.authentication.handler.support.AbstractPac4jAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.HttpAction;

import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which gets the credentials and then the user profile
 * in a delegated authentication process from an external identity provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@SuppressWarnings("unchecked")
@Slf4j
public class ClientAuthenticationHandler extends AbstractPac4jAuthenticationHandler {

    private final Clients clients;

    public ClientAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                       final PrincipalFactory principalFactory,
                                       final Clients clients) {
        super(name, servicesManager, principalFactory, null);
        this.clients = clients;
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && ClientCredential.class.isAssignableFrom(credential.getClass());
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential) throws GeneralSecurityException, PreventedException {
        try {
            val clientCredentials = (ClientCredential) credential;
            LOGGER.debug("Located client credentials as [{}]", clientCredentials);

            val credentials = clientCredentials.getCredentials();
            LOGGER.debug("Client name: [{}]", clientCredentials.getClientName());

            // get client
            val client = this.clients.findClient(clientCredentials.getClientName());
            LOGGER.debug("Delegated client is: [{}]", client);

            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext();
            val webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

            val userProfile = client.getUserProfile(credentials, webContext);
            LOGGER.debug("Final user profile is: [{}]", userProfile);
            return createResult(clientCredentials, userProfile);
        } catch (final HttpAction e) {
            throw new PreventedException(e);
        }
    }
}
