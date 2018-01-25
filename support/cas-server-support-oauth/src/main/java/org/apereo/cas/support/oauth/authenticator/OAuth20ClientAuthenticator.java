package org.apereo.cas.support.oauth.authenticator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuthClientProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;

/**
 * Authenticator for client credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class OAuth20ClientAuthenticator implements Authenticator<UsernamePasswordCredentials> {
    private final OAuth20Validator validator;
    private final ServicesManager servicesManager;
    
    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context)
            throws CredentialsException {
        LOGGER.debug("Authenticating credential [{}]", credentials);
        
        final String id = credentials.getUsername();
        final String secret = credentials.getPassword();
        final OAuthRegisteredService registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, id);

        if (!this.validator.checkServiceValid(registeredService)) {
            throw new CredentialsException("Service invalid for client identifier: " + id);
        }

        if (!this.validator.checkClientSecret(registeredService, secret)) {
            throw new CredentialsException("Bad secret for client identifier: " + id);
        }

        final OAuthClientProfile profile = new OAuthClientProfile();
        profile.setId(id);
        credentials.setUserProfile(profile);
        LOGGER.debug("Authenticated user profile [{}]", profile);
    }
}
