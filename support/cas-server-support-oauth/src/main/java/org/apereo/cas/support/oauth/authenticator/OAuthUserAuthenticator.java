package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuthUserAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthUserAuthenticator.class);

    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final ServicesManager servicesManager;
    private final ServiceFactory webApplicationServiceFactory;

    public OAuthUserAuthenticator(final AuthenticationSystemSupport authenticationSystemSupport,
                                  final ServicesManager servicesManager,
                                  final ServiceFactory webApplicationServiceFactory) {
        this.authenticationSystemSupport = authenticationSystemSupport;
        this.servicesManager = servicesManager;
        this.webApplicationServiceFactory = webApplicationServiceFactory;
    }

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws CredentialsException {
        final UsernamePasswordCredential casCredential = new UsernamePasswordCredential(credentials.getUsername(), credentials.getPassword());
        try {

            final String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
            final Service service = this.webApplicationServiceFactory.createService(clientId);
            final RegisteredService registeredService = OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            final AuthenticationResult authenticationResult = this.authenticationSystemSupport
                    .handleAndFinalizeSingleAuthenticationTransaction(null, casCredential);
            final Authentication authentication = authenticationResult.getAuthentication();
            final Principal principal = authentication.getPrincipal();

            final OAuthUserProfile profile = new OAuthUserProfile();
            final String id = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
            LOGGER.debug("Created profile id [{}]", id);

            profile.setId(id);
            final Map<String, Object> attributes = registeredService.getAttributeReleasePolicy()
                    .getAttributes(principal, service, registeredService);
            profile.addAttributes(attributes);
            LOGGER.debug("Authenticated user profile [{}]", profile);

            credentials.setUserProfile(profile);
        } catch (final Exception e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }
}
