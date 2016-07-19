package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.support.oauth.profile.OAuthUserProfile;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.AbstractUsernamePasswordAuthenticator;
import org.pac4j.core.exception.CredentialsException;

import java.util.Map;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public class OAuthUserAuthenticator extends AbstractUsernamePasswordAuthenticator {
    
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) {
        final UsernamePasswordCredential casCredential = new UsernamePasswordCredential(credentials.getUsername(),
                credentials.getPassword());
        try {

            final AuthenticationResult authenticationResult = this.authenticationSystemSupport
                    .handleAndFinalizeSingleAuthenticationTransaction(null, casCredential);
            final Authentication authentication = authenticationResult.getAuthentication();
            final Principal principal = authentication.getPrincipal();

            final OAuthUserProfile profile = new OAuthUserProfile();
            profile.setId(principal.getId());
            final Map<String, Object> attributes = principal.getAttributes();
            if (attributes != null) {
                profile.addAttributes(attributes);
            }
            credentials.setUserProfile(profile);
        } catch (final AuthenticationException e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }

    public AuthenticationSystemSupport getAuthenticationSystemSupport() {
        return this.authenticationSystemSupport;
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }
}
