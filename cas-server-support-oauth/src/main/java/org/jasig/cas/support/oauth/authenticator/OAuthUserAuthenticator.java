package org.jasig.cas.support.oauth.authenticator;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.oauth.profile.OAuthUserProfile;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.http.credentials.UsernamePasswordCredentials;
import org.pac4j.http.credentials.authenticator.UsernamePasswordAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
@Component("oAuthUserAuthenticator")
public class OAuthUserAuthenticator implements UsernamePasswordAuthenticator {

    @NotNull
    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Override
    public void validate(final UsernamePasswordCredentials credentials) {
        final UsernamePasswordCredential casCredential = new UsernamePasswordCredential(credentials.getUsername(),
                credentials.getPassword());
        try {

            final AuthenticationResult authenticationResult = authenticationSystemSupport
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
        return authenticationSystemSupport;
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }
}
