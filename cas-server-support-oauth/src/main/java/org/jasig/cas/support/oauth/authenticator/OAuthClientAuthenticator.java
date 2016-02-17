package org.jasig.cas.support.oauth.authenticator;

import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.support.oauth.profile.OAuthClientProfile;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.util.OAuthUtils;
import org.jasig.cas.support.oauth.validator.OAuthValidator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * Authenticator for client credentials authentication.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Component("oAuthClientAuthenticator")
public class OAuthClientAuthenticator implements UsernamePasswordAuthenticator {

    /** The OAuth validator. */
    @NotNull
    @Autowired
    @Qualifier("oAuthValidator")
    protected OAuthValidator validator;

    /** The services manager. */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    @Override
    public void validate(final UsernamePasswordCredentials credentials) {
        final String id = credentials.getUsername();
        final String secret = credentials.getPassword();
        final OAuthRegisteredService registeredService = OAuthUtils.getRegisteredOAuthService(this.servicesManager, id);

        if (!validator.checkServiceValid(registeredService)) {
            throw new CredentialsException("Service invalid for client identifier: " + id);
        }

        if (!validator.checkClientSecret(registeredService, secret)) {
            throw new CredentialsException("Bad secret for client identifier: " + id);
        }

        final OAuthClientProfile profile = new OAuthClientProfile();
        profile.setId(id);
        credentials.setUserProfile(profile);
    }

    public OAuthValidator getValidator() {
        return validator;
    }

    public void setValidator(final OAuthValidator validator) {
        this.validator = validator;
    }

    public ServicesManager getServicesManager() {
        return servicesManager;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }
}
