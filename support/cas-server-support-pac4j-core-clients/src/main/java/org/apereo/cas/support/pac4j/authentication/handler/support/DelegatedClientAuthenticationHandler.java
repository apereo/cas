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
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.profile.CommonProfile;

import java.security.GeneralSecurityException;
import java.util.Objects;
import java.util.Optional;

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
    private final SessionStore<JEEContext> sessionStore;

    public DelegatedClientAuthenticationHandler(final String name,
                                                final Integer order,
                                                final ServicesManager servicesManager,
                                                final PrincipalFactory principalFactory,
                                                final Clients clients,
                                                final DelegatedClientUserProfileProvisioner profileProvisioner,
                                                final SessionStore<JEEContext> sessionStore) {
        super(name, servicesManager, principalFactory, order);
        this.clients = clients;
        this.profileProvisioner = profileProvisioner;
        this.sessionStore = sessionStore;
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

            LOGGER.trace("Client name: [{}]", clientCredentials.getClientName());

            val clientResult = clients.findClient(clientCredentials.getClientName());
            if (clientResult.isEmpty()) {
                throw new IllegalArgumentException("Unable to determine client based on client name " + clientCredentials.getClientName());
            }
            val client = BaseClient.class.cast(clientResult.get());
            LOGGER.trace("Delegated client is: [{}]", client);
            
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext();
            val webContext = new JEEContext(Objects.requireNonNull(request),
                Objects.requireNonNull(response), this.sessionStore);

            var userProfileResult = Optional.ofNullable(clientCredentials.getUserProfile());
            if (userProfileResult.isEmpty()) {
                val credentials = clientCredentials.getCredentials();
                userProfileResult = client.getUserProfile(credentials, webContext);
            }
            if (userProfileResult.isEmpty()) {
                throw new PreventedException("Unable to fetch user profile from client " + client.getName());
            }
            val userProfile = userProfileResult.get();
            LOGGER.debug("Final user profile is: [{}]", userProfile);
            storeUserProfile(webContext, userProfile);
            return createResult(clientCredentials, userProfile, client);
        } catch (final HttpAction e) {
            throw new PreventedException(e);
        }
    }

    @Override
    protected void preFinalizeAuthenticationHandlerResult(final ClientCredential credentials, final Principal principal,
                                                          final CommonProfile profile, final BaseClient client) {
        profileProvisioner.execute(principal, profile, client);
    }
}
