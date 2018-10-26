package org.apereo.cas.integration.pac4j.authentication.handler.support;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.handler.PrincipalNameTransformer;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.AccountNotFoundException;
import java.security.GeneralSecurityException;

/**
 * Pac4j authentication handler which works on a CAS username / password credential
 * and uses a pac4j authenticator and profile creator to play authentication.
 *
 * @author Jerome Leleu
 * @since 4.2.0
 */
@Slf4j
@Setter
public class UsernamePasswordWrapperAuthenticationHandler extends AbstractWrapperAuthenticationHandler<UsernamePasswordCredential, UsernamePasswordCredentials> {

    /**
     * The underlying pac4j authenticator.
     */
    protected Authenticator<UsernamePasswordCredentials> authenticator = new SimpleTestUsernamePasswordAuthenticator();

    /**
     * PasswordEncoder to be used by subclasses to encode passwords for
     * comparing against a resource.
     */
    private PasswordEncoder passwordEncoder = NoOpPasswordEncoder.getInstance();

    /**
     * PrincipalNameTransformer to be used by subclasses to transform the principal name.
     */
    private PrincipalNameTransformer principalNameTransformer = formUserId -> formUserId;

    public UsernamePasswordWrapperAuthenticationHandler(final String name, final ServicesManager servicesManager, final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected UsernamePasswordCredentials convertToPac4jCredentials(final UsernamePasswordCredential casCredential) throws GeneralSecurityException {
        LOGGER.debug("CAS credentials: [{}]", casCredential);
        val username = this.principalNameTransformer.transform(casCredential.getUsername());
        if (username == null) {
            throw new AccountNotFoundException("Username is null.");
        }
        val password = this.passwordEncoder.encode(casCredential.getPassword());
        val credentials = new UsernamePasswordCredentials(username, password);
        LOGGER.debug("pac4j credentials: [{}]", credentials);
        return credentials;
    }

    @Override
    protected Authenticator<UsernamePasswordCredentials> getAuthenticator(final Credential credential) {
        return this.authenticator;
    }

    @Override
    public boolean supports(final Class<? extends Credential> clazz) {
        return UsernamePasswordCredential.class.isAssignableFrom(clazz);
    }

    @Override
    protected Class<UsernamePasswordCredential> getCasCredentialsType() {
        return UsernamePasswordCredential.class;
    }
}
