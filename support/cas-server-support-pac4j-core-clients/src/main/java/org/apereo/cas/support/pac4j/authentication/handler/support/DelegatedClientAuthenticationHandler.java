package org.apereo.cas.support.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.PreventedException;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.provision.DelegatedClientUserProfileProvisioner;
import org.apereo.cas.integration.pac4j.authentication.handler.support.AbstractPac4jAuthenticationHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.Pac4jUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.profile.UserProfile;

import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which gets the credentials and then the user profile
 * in a delegated authentication process from an external identity provider.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
public class DelegatedClientAuthenticationHandler extends AbstractPac4jAuthenticationHandler {

    private final Clients clients;
    private final DelegatedClientUserProfileProvisioner profileProvisioner;

    public DelegatedClientAuthenticationHandler(final String name, final ServicesManager servicesManager,
                                                final PrincipalFactory principalFactory,
                                                final Clients clients, final DelegatedClientUserProfileProvisioner profileProvisioner) {
        super(name, servicesManager, principalFactory, null);
        this.clients = clients;
        this.profileProvisioner = profileProvisioner;
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

            val client = (BaseClient) this.clients.findClient(clientCredentials.getClientName());
            LOGGER.debug("Delegated client is: [{}]", client);

            if (client == null) {
                throw new IllegalArgumentException("Unable to determine client based on client name " + clientCredentials.getClientName());
            }

            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext();
            val webContext = Pac4jUtils.getPac4jJ2EContext(request, response);

            val userProfile = client.getUserProfile(credentials, webContext);
            LOGGER.debug("Final user profile is: [{}]", userProfile);
            return createResult(clientCredentials, userProfile, client);
        } catch (final HttpAction e) {
            throw new PreventedException(e);
        }
    }

    @Override
    protected void preFinalizeAuthenticationHandlerResult(final ClientCredential credentials, final Principal principal,
                                                          final UserProfile profile, final BaseClient client) {
        profileProvisioner.execute(principal, profile, client);
    }
}
