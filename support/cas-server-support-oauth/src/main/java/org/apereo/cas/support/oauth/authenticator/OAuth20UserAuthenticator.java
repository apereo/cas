package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20UserAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;
    private final ServiceFactory webApplicationServiceFactory;

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws CredentialsException {
        val casCredential = new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
        try {
            val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
            val service = this.webApplicationServiceFactory.createService(clientId);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            val authenticationResult = this.authenticationSystemSupport
                .handleAndFinalizeSingleAuthenticationTransaction(null, casCredential);
            val authentication = authenticationResult.getAuthentication();
            val principal = authentication.getPrincipal();

            val profile = new CommonProfile();
            val id = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
            LOGGER.debug("Created profile id [{}]", id);

            profile.setId(id);
            val attributes = registeredService.getAttributeReleasePolicy().getAttributes(principal, service, registeredService);
            profile.addAttributes(attributes);
            LOGGER.debug("Authenticated user profile [{}]", profile);
            credentials.setUserProfile(profile);
        } catch (final Exception e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }
}
